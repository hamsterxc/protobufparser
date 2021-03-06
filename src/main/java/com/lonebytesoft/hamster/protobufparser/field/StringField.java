package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;

// string in .proto
public class StringField extends Field<String> {

    private final String value;

    public StringField(final long tag, final String value) {
        super(tag);
        this.value = value;
    }

    @Override
    public DataType getDataType() {
        return DataType.STRING;
    }

    @Override
    public String getValue() {
        return value;
    }

}
