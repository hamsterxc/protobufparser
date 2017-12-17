package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// float in .proto
public class FloatField extends Field<Float> {

    private final float value;

    public FloatField(final String name, final float value) {
        super(name);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.FLOAT;
    }

    @Override
    public Float getValue() {
        return value;
    }

}
