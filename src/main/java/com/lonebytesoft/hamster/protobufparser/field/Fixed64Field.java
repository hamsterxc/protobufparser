package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// fixed64, sfixed64 in .proto
public class Fixed64Field extends Field<Long> {

    private final long value;

    public Fixed64Field(final String name, final long value) {
        super(name);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.FIXED_64;
    }

    @Override
    public Long getValue() {
        return value;
    }

}
