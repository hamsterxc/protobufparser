package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.WireType;
import com.lonebytesoft.hamster.protobufparser.field.BytesField;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.FieldFactory;
import com.lonebytesoft.hamster.protobufparser.field.ObjectField;
import com.lonebytesoft.hamster.protobufparser.field.StringField;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmartParserImpl extends AbstractParser {

    private final FieldFactory fieldFactory;

    public SmartParserImpl(final FieldFactory fieldFactory) {
        this.fieldFactory = fieldFactory;
    }

    @Override
    public List<Field<?>> parse(InputStream input, Collection<Definition> definitions) throws IOException {
        final List<Field<?>> fields = super.parse(input, definitions);

        final Map<String, PossibleDataTypes> possibleDataTypesContainer = calculatePossibleDataTypes(fields);
        return compactGenericVarLenFields(fields, possibleDataTypesContainer);
    }

    @Override
    protected Field<?> readField(InputStream input, Definition definition, boolean isGeneric) throws IOException {
        Field<?> field = fieldFactory.buildField(definition, input);

        if(isGeneric && (definition.getDataType().getWireType() == WireType.VAR_LENGTH)) {
            final byte[] data = ((BytesField) field).getValue();
            field = new GenericVarLenField(definition.getTag());

            for(final DataType dataTypeGuess : GenericVarLenField.DATA_TYPES) {
                final Field<?> fieldGeneric;

                try {
                    final Definition definitionGeneric = new Definition(definition.getName(), definition.getTag(), dataTypeGuess);
                    if(definitionGeneric.getDataType() == DataType.OBJECT) {
                        fieldGeneric = buildObject(data, definitionGeneric.getTag(), definitionGeneric.getChildren());
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

        if(!isGeneric && (definition.getDataType() == DataType.OBJECT)) {
            final byte[] data = ((BytesField) field).getValue();
            field = buildObject(data, field.getTag(), definition.getChildren());
        }

        return field;
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
