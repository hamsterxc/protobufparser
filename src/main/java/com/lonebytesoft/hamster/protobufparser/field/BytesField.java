package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

// bytes in .proto
public class BytesField extends Field<byte[]> {

    private final byte[] value;

    public BytesField(final long tag, final byte[] value) {
        super(tag);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.BYTES;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "#" + getTag() + " = " +
                "[" +
                IntStream.range(0, value.length)
                        .map(index -> Byte.toUnsignedInt(value[index]))
                        .mapToObj(value -> "0x" + (Integer.toHexString(value / 0x10) + Integer.toHexString(value % 0x10)).toUpperCase())
                        .collect(Collectors.joining(", "))
                + "]";
    }

}
