package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackedIntField extends Field<List<Long>> {

    private final List<Long> value;

    public PackedIntField(final long tag, final List<Long> value) {
        super(tag);
        this.value = Collections.unmodifiableList(new ArrayList<>(value));
    }

    @Override
    public DataType getDataType() {
        return DataType.PACKED_INT;
    }

    @Override
    public List<Long> getValue() {
        return value;
    }

}
