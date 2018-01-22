package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.WireType;

import java.io.IOException;

class Key {

    private final WireType wireType;
    private final long tag;

    public Key(final WireType wireType, final long tag) {
        this.tag = tag;
        this.wireType = wireType;
    }

    public WireType getWireType() {
        return wireType;
    }

    public long getTag() {
        return tag;
    }

    public static Key parse(final long value) throws IOException {
        final long typeCode = value & 0x07;
        final WireType type = WireType.parse(typeCode)
                .orElseThrow(() -> new IOException("Could not parse key type " + typeCode));

        final long tag = value >> 3;

        return new Key(type, tag);
    }

    @Override
    public String toString() {
        return "Key{" +
                "wireType=" + wireType +
                ", tag=" + tag +
                '}';
    }

}
