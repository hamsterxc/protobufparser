package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// double in .proto
public class DoubleField extends Field<Double> {

    private final double value;

    public DoubleField(final String name, final double value) {
        super(name);
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
