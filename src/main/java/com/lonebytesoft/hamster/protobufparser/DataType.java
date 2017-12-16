package com.lonebytesoft.hamster.protobufparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum DataType {

    INTEGER(WireType.VAR_INT) {
        @Override
        public Long read(InputStream input) throws IOException {
            return Reader.convertToNumber(getWireType().readData(input));
        }
    },

    BOOL(WireType.VAR_INT) {
        @Override
        public Boolean read(InputStream input) throws IOException {
            return ((Long) INTEGER.read(input)) == 1;
        }
    },

    DOUBLE(WireType.BIT_64) {
        @Override
        public Double read(InputStream input) throws IOException {
            return Double.longBitsToDouble(Reader.convertToNumber(getWireType().readData(input)));
        }
    },

    INT_64(WireType.BIT_64) {
        @Override
        public Long read(InputStream input) throws IOException {
            return Reader.convertToNumber(getWireType().readData(input));
        }
    },

    STRING(WireType.VAR_LENGTH) {
        @Override
        public String read(InputStream input) throws IOException {
            return new String(getWireType().readData(input));
        }
    },

    BYTES(WireType.VAR_LENGTH) {
        @Override
        public byte[] read(InputStream input) throws IOException {
            return getWireType().readData(input);
        }

        @Override
        public String readToString(Object read) {
            return DataType.byteArrayToHexString((byte[]) read);
        }
    },

    OBJECT(WireType.VAR_LENGTH) {
        @Override
        public byte[] read(InputStream input) throws IOException {
            return getWireType().readData(input);
        }

        @Override
        public String readToString(Object read) {
            return DataType.byteArrayToHexString((byte[]) read);
        }
    },

    PACKED_INTEGER(WireType.VAR_LENGTH) {
        @Override
        public List<Long> read(InputStream input) throws IOException {
            final byte[] data = (byte[]) BYTES.read(input);
            final PushbackInputStream subInput = new PushbackInputStream(new ByteArrayInputStream(data));
            final List<Long> result = new ArrayList<>();
            while(Reader.hasMore(subInput)) {
                result.add((Long) INTEGER.read(subInput));
            }
            return result;
        }
    },

    FLOAT(WireType.BIT_32) {
        @Override
        public Float read(InputStream input) throws IOException {
            return Float.intBitsToFloat((int) Reader.convertToNumber(getWireType().readData(input)));
        }
    },

    INT_32(WireType.BIT_32) {
        @Override
        public Integer read(InputStream input) throws IOException {
            return (int) Reader.convertToNumber(getWireType().readData(input));
        }
    },

    ;

    private final WireType wireType;

    DataType(final WireType wireType) {
        this.wireType = wireType;
    }

    public abstract Object read(InputStream input) throws IOException;

    public String readToString(final Object read) {
        return String.valueOf(read);
    }

    public WireType getWireType() {
        return wireType;
    }

    @Override
    public String toString() {
        return "DataType." + name() + "(" + wireType + ')';
    }

    private static String byteArrayToHexString(byte[] bytes) {
        return "[" +
                IntStream.range(0, bytes.length)
                        .map(index -> Byte.toUnsignedInt(bytes[index]))
                        .mapToObj(value -> "0x" + (Integer.toHexString(value / 0x10) + Integer.toHexString(value % 0x10)).toUpperCase())
                        .collect(Collectors.joining(", "))
                + "]";
    }

}
