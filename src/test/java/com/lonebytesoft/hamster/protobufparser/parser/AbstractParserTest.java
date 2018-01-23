package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.field.Field;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

abstract class AbstractParserTest {

    protected abstract Parser getParser();

    protected byte[] data(final int... bytes) {
        final byte[] data = new byte[bytes.length];
        IntStream.range(0, bytes.length).forEach(index -> data[index] = (byte) bytes[index]);
        return data;
    }

    protected List<Field<?>> parse(final byte[] data, final Collection<Definition> definitions) throws IOException {
        return getParser().parse(new ByteArrayInputStream(data), definitions);
    }

}
