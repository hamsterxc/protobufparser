package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// bool in .proto
public class BoolField extends Field<Boolean> {

    private final boolean value;

    public BoolField(final String name, final boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.BOOL;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

}
