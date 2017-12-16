package com.lonebytesoft.hamster.protobufparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

enum WireType {

    VAR_INT(0) {
        @Override
        public byte[] readData(InputStream input) throws IOException {
            return Reader.readVarintBytes(input);
        }
    },

    BIT_64(1) {
        @Override
        public byte[] readData(InputStream input) throws IOException {
            return Reader.readBytes(input, 8);
        }
    },

    VAR_LENGTH(2) {
        @Override
        public byte[] readData(InputStream input) throws IOException {
            final long length = Reader.readVarint(input);
            // todo: reading max Integer.MAX_VALUE bytes
            return Reader.readBytes(input, (int) length);
        }
    },

    BIT_32(5) {
        @Override
        public byte[] readData(InputStream input) throws IOException {
            return Reader.readBytes(input, 4);
        }
    },
    ;

    private final long code;

    WireType(final long code) {
        this.code = code;
    }

    public abstract byte[] readData(InputStream input) throws IOException;

    public static Optional<WireType> parse(final long code) {
        return Arrays.stream(values())
                .filter(wireType -> wireType.code == code)
                .findFirst();
    }

}
