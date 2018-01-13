package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// int32, int64, uint32, uint64 in .proto
public class IntField extends Field<Long> {

    private final Long value;

    public IntField(final long tag, final Long value) {
        super(tag);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.INT;
    }

    @Override
    public Long getValue() {
        return value;
    }

}
