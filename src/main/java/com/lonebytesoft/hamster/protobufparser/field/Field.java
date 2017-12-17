package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

public abstract class Field<T> {

    private final String name;

    protected Field(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract DataType getDataType();

    public abstract T getValue();

    @Override
    public String toString() {
        return getName() + " = " + getValue();
    }

}
