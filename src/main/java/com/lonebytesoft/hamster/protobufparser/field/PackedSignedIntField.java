package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackedSignedIntField extends Field<List<Long>> {

    private final List<Long> value;

    public PackedSignedIntField(final String name, final List<Long> value) {
        super(name);
        this.value = Collections.unmodifiableList(new ArrayList<>(value));
    }

    @Override
    public DataType getDataType() {
        return DataType.PACKED_SIGNED_INT;
    }

    @Override
    public List<Long> getValue() {
        return value;
    }

}
