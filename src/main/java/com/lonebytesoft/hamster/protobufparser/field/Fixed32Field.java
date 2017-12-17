package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// fixed32, sfixed32 in .proto
public class Fixed32Field extends Field<Integer> {

    private final int value;

    public Fixed32Field(final String name, final int value) {
        super(name);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.FIXED_32;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
