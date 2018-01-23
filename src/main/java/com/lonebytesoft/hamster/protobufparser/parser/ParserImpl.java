package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.field.BytesField;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.FieldFactory;

import java.io.IOException;
import java.io.InputStream;

public class ParserImpl extends AbstractParser {

    private final FieldFactory fieldFactory;

    public ParserImpl(final FieldFactory fieldFactory) {
        this.fieldFactory = fieldFactory;
    }

    @Override
    protected Field<?> readField(InputStream input, Definition definition, boolean isGeneric) throws IOException {
        final Field<?> field = fieldFactory.buildField(definition, input);

        if(definition.getDataType() == DataType.OBJECT) {
            final byte[] data = ((BytesField) field).getValue();
            return buildObject(data, field.getTag(), definition.getChildren());
        } else {
            return field;
        }
    }

}
