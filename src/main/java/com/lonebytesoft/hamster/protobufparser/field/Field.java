package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

public abstract class Field<T> {

    private final long tag;

    protected Field(final long tag) {
        this.tag = tag;
    }

    public long getTag() {
        return tag;
    }

    public abstract DataType getDataType();

    public abstract T getValue();

    @Override
    public String toString() {
        return "#" + getTag() + " = " + getValue();
    }

}
