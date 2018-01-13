package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// sint32, sint64 in .proto
public class SignedIntField extends Field<Long> {

    private final Long value;

    public SignedIntField(final long tag, final Long value) {
        super(tag);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.SIGNED_INT;
    }

    @Override
    public Long getValue() {
        return value;
    }

}
