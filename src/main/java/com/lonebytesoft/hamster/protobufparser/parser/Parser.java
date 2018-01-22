package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.Reader;
import com.lonebytesoft.hamster.protobufparser.WireType;
import com.lonebytesoft.hamster.protobufparser.field.BytesField;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.FieldFactory;
import com.lonebytesoft.hamster.protobufparser.field.ObjectField;
import com.lonebytesoft.hamster.protobufparser.field.StringField;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Parser {

    private static final Map<WireType, DataType> GENERIC_DATA_TYPE = Collections.unmodifiableMap(new HashMap<WireType, DataType>() {{
        put(WireType.VAR_INT, DataType.SIGNED_INT);
        put(WireType.BIT_64, DataType.FIXED_64);
        put(WireType.VAR_LENGTH, DataType.BYTES);
        put(WireType.BIT_32, DataType.FIXED_32);
    }});

    private final FieldFactory fieldFactory;

    public Parser(final FieldFactory fieldFactory) {
        this.fieldFactory = fieldFactory;
    }

    public List<Field<?>> parse(final InputStream input, final Collection<Definition> definitions) throws IOException {
        return parse(input, definitions, false);
    }

    public List<Field<?>> parse(final InputStream input, final Collection<Definition> definitions,
                                final boolean isSmartVarLength) throws IOException {
        final List<Field<?>> fields = parse(new PushbackInputStream(input), Definition.mapChildren(definitions), isSmartVarLength);

        if(isSmartVarLength) {
            final Map<String, PossibleDataTypes> possibleDataTypesContainer = calculatePossibleDataTypes(fields);
            return compactGenericVarLenFields(fields, possibleDataTypesContainer);
        } else {
            return fields;
        }
    }

    private List<Field<?>> parse(final PushbackInputStream input, final Map<Long, Definition> definitions,
                                 final boolean isSmartVarLength) throws IOException {
        final List<Field<?>> result = new ArrayList<>();

        while(Reader.hasMore(input)) {
            final Key key = Key.parse(Reader.readVarint(input));
            final WireType wireType = key.getWireType();

            final Definition definitionKnown = definitions.get(key.getTag());
            final boolean isGeneric = definitionKnown == null;
            final Definition definition;
            if(isGeneric) {
                //throw new IOException("Definition not found for tag #" + key.getTag()));
                definition = new Definition(null, key.getTag(), GENERIC_DATA_TYPE.get(wireType));
            } else {
                definition = definitionKnown;
            }

            final DataType dataType = definition.getDataType();
            if(dataType.getWireType() != wireType) {
                throw new IOException("Definition and key wire type mismatch for " + key + ", " + definition);
            }

            Field<?> field = fieldFactory.buildField(definition, input);

            if(isSmartVarLength && isGeneric && (wireType == WireType.VAR_LENGTH)) {
                final byte[] data = ((BytesField) field).getValue();
                field = new GenericVarLenField(definition.getTag());

                for(final DataType dataTypeGuess : GenericVarLenField.DATA_TYPES) {
                    final Field<?> fieldGeneric;

                    try {
                        final Definition definitionGeneric = new Definition(definition.getName(), definition.getTag(), dataTypeGuess);
                        if(definitionGeneric.getDataType() == DataType.OBJECT) {
                            fieldGeneric = buildObject(data, definitionGeneric.getTag(), definitionGeneric.getChildren(), isSmartVarLength);
                        } else {
                            fieldGeneric = fieldFactory.buildField(definitionGeneric, data);
                        }

                        if((definitionGeneric.getDataType() == DataType.STRING)
                                && !isNiceStringHeuristic(((StringField) fieldGeneric).getValue())) {
                            continue;
                        }
                    } catch(IOException ignored) {
                        continue;
                    }

                    ((GenericVarLenField) field).setField(fieldGeneric);
                }
            }

            if(!(isSmartVarLength && isGeneric) && (definition.getDataType() == DataType.OBJECT)) {
                final byte[] data = ((BytesField) field).getValue();
                field = buildObject(data, field.getTag(), definition.getChildren(), isSmartVarLength);
            }

            result.add(field);
        }

        return result;
    }

    private Field<?> buildObject(final byte[] data, final long tag, final Map<Long, Definition> definitions,
                                 final boolean isSmartVarLength) throws IOException {
        final PushbackInputStream subInput = new PushbackInputStream(new ByteArrayInputStream(data));
        return new ObjectField(tag, parse(subInput, definitions, isSmartVarLength));
    }

    private boolean isNiceStringHeuristic(final String s) {
        return s.chars()
                .noneMatch(c -> c < 0x20);
    }

    private Map<String, PossibleDataTypes> calculatePossibleDataTypes(final List<Field<?>> fields) {
        final Map<String, PossibleDataTypes> possibleDataTypesContainer = new HashMap<>();
        calculatePossibleDataTypes(fields, new ArrayList<>(), possibleDataTypesContainer);
        return possibleDataTypesContainer;
    }

    private void calculatePossibleDataTypes(final List<Field<?>> fields, final List<Long> path,
                                            final Map<String, PossibleDataTypes> possibleDataTypesContainer) {
        fields.forEach(field -> {
            path.add(field.getTag());
            final String key = buildPathKey(path);

            if(field instanceof GenericVarLenField) {
                final GenericVarLenField genericVarLenField = (GenericVarLenField) field;

                final PossibleDataTypes possibleDataTypesNew = genericVarLenField.calculatePossibleDataTypes();
                final PossibleDataTypes possibleDataTypes = possibleDataTypesContainer.compute(key,
                        (pathKey, possibleDataTypesExisting) -> possibleDataTypesNew.merge(possibleDataTypesExisting));

                final Field<?> fieldUnderlying = genericVarLenField.getField(possibleDataTypes.getFirstPossibleDataType());
                if(fieldUnderlying instanceof ObjectField) {
                    calculatePossibleDataTypes(((ObjectField) fieldUnderlying).getValue(), path, possibleDataTypesContainer);
                }
            } else if(field instanceof ObjectField) {
                calculatePossibleDataTypes(((ObjectField) field).getValue(), path, possibleDataTypesContainer);
            }

            path.remove(path.size() - 1);
        });
    }

    private List<Field<?>> compactGenericVarLenFields(final List<Field<?>> fields,
                                                      final Map<String, PossibleDataTypes> possibleDataTypesContainer) {
        return compactGenericVarLenFields(fields, new ArrayList<>(), possibleDataTypesContainer);
    }

    private List<Field<?>> compactGenericVarLenFields(final List<Field<?>> fields, final List<Long> path,
                                                      final Map<String, PossibleDataTypes> possibleDataTypesContainer) {
        return fields.stream()
                .map(field -> {
                    if((field instanceof GenericVarLenField) || (field instanceof ObjectField)) {
                        path.add(field.getTag());

                        Field<?> compacted;
                        if(field instanceof GenericVarLenField) {
                            final String key = buildPathKey(path);
                            final PossibleDataTypes possibleDataTypes = possibleDataTypesContainer.get(key);
                            if(possibleDataTypes == null) {
                                throw new IllegalArgumentException("Unknown generic var-length field possible data types, path " + key);
                            }

                            compacted = ((GenericVarLenField) field).getField(possibleDataTypes.getFirstPossibleDataType());
                        } else {
                            compacted = field;
                        }

                        if(compacted instanceof ObjectField) {
                            compacted = new ObjectField(compacted.getTag(),
                                    compactGenericVarLenFields(((ObjectField) compacted).getValue(), path, possibleDataTypesContainer));
                        }

                        path.remove(path.size() - 1);

                        return compacted;
                    } else {
                        return field;
                    }
                })
                .collect(Collectors.toList());
    }

    private String buildPathKey(final List<Long> path) {
        return path.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
    }

}
