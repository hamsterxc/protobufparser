package com.lonebytesoft.hamster.protobufparser;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private static final Map<WireType, DataType> GENERIC_DATA_TYPE = Collections.unmodifiableMap(new HashMap<WireType, DataType>() {{
        put(WireType.VAR_INT, DataType.SIGNED_INT);
        put(WireType.BIT_64, DataType.FIXED_64);
        put(WireType.VAR_LENGTH, DataType.BYTES);
        put(WireType.BIT_32, DataType.FIXED_32);
    }});
    private static final List<DataType> SMART_VAR_LENGTH_TYPES = Arrays.asList(
            DataType.OBJECT,
            DataType.STRING,
            DataType.PACKED_SIGNED_INT,
            DataType.BYTES
    );

    private final FieldFactory fieldFactory;

    public Parser(final FieldFactory fieldFactory) {
        this.fieldFactory = fieldFactory;
    }

    public List<Field<?>> parse(final InputStream input, final Collection<Definition> definitions) throws IOException {
        return parse(input, definitions, false);
    }

    public List<Field<?>> parse(final InputStream input, final Collection<Definition> definitions,
                                final boolean isSmartVarLength) throws IOException {
        return parse(new PushbackInputStream(input), Definition.mapChildren(definitions), isSmartVarLength);
    }

    private List<Field<?>> parse(final PushbackInputStream input, final Map<Long, Definition> definitions,
                                 final boolean isSmartVarLength) throws IOException {
        final List<Field<?>> result = new ArrayList<>();

        while(Reader.hasMore(input)) {
            final Key key = Key.parse(Reader.readVarint(input));
            final WireType wireType = key.getWireType();

            final Definition definitionKnown = definitions.get(key.getTag());
            final boolean isGeneric = definitionKnown == null;
            Definition definition;
            if(isGeneric) {
                //throw new IOException("Definition not found for tag #" + key.getTag()));
                definition = new Definition("<#" + key.getTag() + " " + wireType + ">", -1, GENERIC_DATA_TYPE.get(wireType));
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
                for(final DataType dataTypeGuess : SMART_VAR_LENGTH_TYPES) {
                    try {
                        definition = new Definition(definition.getName(), definition.getTag(), dataTypeGuess);
                        field = fieldFactory.buildField(definition, data);

                        if(definition.getDataType() == DataType.OBJECT) {
                            field = buildObject(data, field.getName(), definition.getChildren(), isSmartVarLength);
                        }

                        if((definition.getDataType() == DataType.STRING)
                                && !isNiceStringHeuristic(((StringField) field).getValue())) {
                            continue;
                        }

                        break;
                    } catch(IOException ignored) {
                    }
                }
            }

            if(!(isSmartVarLength && isGeneric) && (definition.getDataType() == DataType.OBJECT)) {
                final byte[] data = ((BytesField) field).getValue();
                field = buildObject(data, field.getName(), definition.getChildren(), isSmartVarLength);
            }

            result.add(field);
        }

        return result;
    }

    private Field<?> buildObject(final byte[] data, final String name, final Map<Long, Definition> definitions,
                                 final boolean isSmartVarLength) throws IOException {
        final PushbackInputStream subInput = new PushbackInputStream(new ByteArrayInputStream(data));
        return new ObjectField(name, parse(subInput, definitions, isSmartVarLength));
    }

    private boolean isNiceStringHeuristic(final String s) {
        return s.chars()
                .noneMatch(c -> c < 0x20);
    }

}
