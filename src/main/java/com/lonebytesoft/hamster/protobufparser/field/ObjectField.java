package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// embedded messages
public class ObjectField extends Field<List<Field<?>>> {

    private final List<Field<?>> value;

    public ObjectField(final String name, final List<Field<?>> value) {
        super(name);
        this.value = Collections.unmodifiableList(new ArrayList<>(value));
    }

    @Override
    public DataType getDataType() {
        return DataType.OBJECT;
    }

    @Override
    public List<Field<?>> getValue() {
        return value;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

}
