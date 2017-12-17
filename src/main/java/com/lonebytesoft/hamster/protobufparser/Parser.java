package com.lonebytesoft.hamster.protobufparser;

import com.lonebytesoft.hamster.protobufparser.field.BytesField;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.FieldFactory;
import com.lonebytesoft.hamster.protobufparser.field.ObjectField;

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
        return parse(new PushbackInputStream(input), Definition.mapChildren(definitions));
    }

    private List<Field<?>> parse(final PushbackInputStream input, final Map<Long, Definition> definitions) throws IOException {
        final List<Field<?>> result = new ArrayList<>();

        while(Reader.hasMore(input)) {
            final Key key = Key.parse(Reader.readVarint(input));
            final Definition definitionKnown = definitions.get(key.getTag());
            final boolean isGeneric = definitionKnown == null;
            final Definition definition;
            if(isGeneric) {
                //throw new IOException("Definition not found for tag #" + key.getTag()));
                definition = new Definition("<#" + key.getTag() + " " + key.getWireType() + ">", -1, GENERIC_DATA_TYPE.get(key.getWireType()));
            } else {
                definition = definitionKnown;
            }

            final DataType dataType = definition.getDataType();
            if(dataType.getWireType() != key.getWireType()) {
                throw new IOException("Definition and key wire type mismatch for " + key + ", " + definition);
            }

            final Field<?> field;
            final Field<?> fieldRead = fieldFactory.buildField(definition, input);
            // special case of embedded messages
            if(definition.getDataType() == DataType.OBJECT) {
                final byte[] data = ((BytesField) fieldRead).getValue();
                final PushbackInputStream subInput = new PushbackInputStream(new ByteArrayInputStream(data));
                field = new ObjectField(fieldRead.getName(), parse(subInput, definition.getChildren()));
            } else {
                field = fieldRead;
            }

            result.add(field);
        }

        return result;
    }

}
