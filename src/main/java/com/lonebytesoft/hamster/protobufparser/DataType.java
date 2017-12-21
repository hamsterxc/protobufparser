package com.lonebytesoft.hamster.protobufparser;

public enum DataType {

    DOUBLE("double", WireType.BIT_64),
    FLOAT("float", WireType.BIT_32),

    INT("int64", WireType.VAR_INT),
    SIGNED_INT("sint64", WireType.VAR_INT),

    FIXED_32("fixed32", WireType.BIT_32),
    FIXED_64("fixed64", WireType.BIT_64),

    BOOL("bool", WireType.VAR_INT),

    STRING("string", WireType.VAR_LENGTH),
    BYTES("bytes", WireType.VAR_LENGTH),
    OBJECT("message", WireType.VAR_LENGTH),

    PACKED_INT(DataType.INT.type, WireType.VAR_LENGTH, DataType.INT),
    PACKED_SIGNED_INT(DataType.SIGNED_INT.type, WireType.VAR_LENGTH, DataType.SIGNED_INT),
    PACKED_BOOL(DataType.BOOL.type, WireType.VAR_LENGTH, DataType.BOOL),

    ;

    private final String type;
    private final WireType wireType;
    private final DataType packedMemberDataType;

    DataType(final String type, final WireType wireType) {
        this(type, wireType, null);
    }

    DataType(final String type, final WireType wireType, final DataType packedMemberDataType) {
        this.type = type;
        this.wireType = wireType;
        this.packedMemberDataType = packedMemberDataType;
    }

    public String getType() {
        return type;
    }

    public WireType getWireType() {
        return wireType;
    }

    public DataType getPackedMemberDataType() {
        return packedMemberDataType;
    }

    @Override
    public String toString() {
        return "DataType." + name() + "(" + wireType + ")=" + type;
    }

}
