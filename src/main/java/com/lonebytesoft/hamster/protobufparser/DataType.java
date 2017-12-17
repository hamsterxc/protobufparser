package com.lonebytesoft.hamster.protobufparser;

public enum DataType {

    DOUBLE(WireType.BIT_64),
    FLOAT(WireType.BIT_32),

    INT(WireType.VAR_INT),
    SIGNED_INT(WireType.VAR_INT),

    FIXED_32(WireType.BIT_32),
    FIXED_64(WireType.BIT_64),

    BOOL(WireType.VAR_INT),

    STRING(WireType.VAR_LENGTH),
    BYTES(WireType.VAR_LENGTH),
    OBJECT(WireType.VAR_LENGTH),

    PACKED_INT(WireType.VAR_LENGTH, DataType.INT),
    PACKED_SIGNED_INT(WireType.VAR_LENGTH, DataType.SIGNED_INT),
    PACKED_BOOL(WireType.VAR_LENGTH, DataType.BOOL),

    ;

    private final WireType wireType;
    private final DataType packedMemberDataType;

    DataType(final WireType wireType) {
        this(wireType, null);
    }

    DataType(final WireType wireType, final DataType packedMemberDataType) {
        this.wireType = wireType;
        this.packedMemberDataType = packedMemberDataType;
    }

    public WireType getWireType() {
        return wireType;
    }

    public DataType getPackedMemberDataType() {
        return packedMemberDataType;
    }

    @Override
    public String toString() {
        return "DataType." + name() + "(" + wireType + ')';
    }

}
