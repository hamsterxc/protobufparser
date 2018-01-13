package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// double in .proto
public class DoubleField extends Field<Double> {

    private final double value;

    public DoubleField(final long tag, final double value) {
        super(tag);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.DOUBLE;
    }

    @Override
    public Double getValue() {
        return value;
    }

}
