package com.lonebytesoft.hamster.protobufparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    private static final Map<WireType, DataType> GENERIC_DATA_TYPE = Collections.unmodifiableMap(new HashMap<WireType, DataType>() {{
        put(WireType.VAR_INT, DataType.INTEGER);
        put(WireType.BIT_64, DataType.INT_64);
        put(WireType.VAR_LENGTH, DataType.BYTES);
        put(WireType.BIT_32, DataType.INT_32);
    }});

    public void parse(final InputStream input, final Definition root) throws IOException {
        parse(new PushbackInputStream(input), root.getChildren(), "");
    }

    private void parse(final PushbackInputStream input, final Map<Long, Definition> definitions, final String indent) throws IOException {
        while(Reader.hasMore(input)) {
            final Key key = Key.parse(Reader.readVarint(input));
            final Definition definitionKnown = definitions.get(key.getTag());
            final boolean isGeneric = definitionKnown == null;
            final Definition definition;
            if(isGeneric) {
                //throw new IOException("Definition not found for tag #" + key.getTag()));
                definition = new Definition(null, -1, GENERIC_DATA_TYPE.get(key.getWireType()));
            } else {
                definition = definitionKnown;
            }

            final DataType dataType = definition.getDataType();
            if(dataType.getWireType() != key.getWireType()) {
                throw new IOException("Definition and key wire type mismatch for " + key + ", " + definition);
            }

            final Object object = dataType.read(input);
            if(dataType == DataType.OBJECT) {
                System.out.println(indent + definition.getName());
                parse(new PushbackInputStream(new ByteArrayInputStream((byte[]) object)), definition.getChildren(), indent + "  ");
            } else {
                final String name;
                if(isGeneric) {
                    name = "<#" + key.getTag() + " " + key.getWireType() + ">";
                } else {
                    name = definition.getName();
                }
                System.out.println(indent + name + " = " + dataType.readToString(object));
            }
        }
    }

}
