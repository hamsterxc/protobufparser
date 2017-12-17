package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackedBoolField extends Field<List<Boolean>> {

    private final List<Boolean> value;

    public PackedBoolField(final String name, final List<Boolean> value) {
        super(name);
        this.value = Collections.unmodifiableList(new ArrayList<>(value));
    }

    @Override
    public DataType getDataType() {
        return DataType.PACKED_BOOL;
    }

    @Override
    public List<Boolean> getValue() {
        return value;
    }

}
