package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.Reader;
import com.lonebytesoft.hamster.protobufparser.WireType;
import com.lonebytesoft.hamster.protobufparser.field.Field;
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

abstract class AbstractParser implements Parser {

    private static final Map<WireType, DataType> GENERIC_DATA_TYPE = Collections.unmodifiableMap(new HashMap<WireType, DataType>() {{
        put(WireType.VAR_INT, DataType.SIGNED_INT);
        put(WireType.BIT_64, DataType.FIXED_64);
        put(WireType.VAR_LENGTH, DataType.BYTES);
        put(WireType.BIT_32, DataType.FIXED_32);
    }});

    @Override
    public List<Field<?>> parse(InputStream input, Collection<Definition> definitions) throws IOException {
        return parse(new PushbackInputStream(input), Definition.mapChildren(definitions));
    }

    private List<Field<?>> parse(final PushbackInputStream input, final Map<Long, Definition> definitions) throws IOException {
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

            final Field<?> field = readField(input, definition, isGeneric);
            result.add(field);
        }

        return result;
    }

    protected Field<?> buildObject(final byte[] data, final long tag, final Map<Long, Definition> definitions) throws IOException {
        final PushbackInputStream subInput = new PushbackInputStream(new ByteArrayInputStream(data));
        final List<Field<?>> children = parse(subInput, definitions);
        return new ObjectField(tag, children);
    }

    protected abstract Field<?> readField(InputStream input, Definition definition, boolean isGeneric) throws IOException;

}
