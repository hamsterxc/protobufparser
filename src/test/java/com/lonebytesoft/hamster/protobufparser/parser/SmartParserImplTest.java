package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.FieldFactory;
import com.lonebytesoft.hamster.protobufparser.field.ObjectField;
import com.lonebytesoft.hamster.protobufparser.field.PackedSignedIntField;
import com.lonebytesoft.hamster.protobufparser.field.SignedIntField;
import com.lonebytesoft.hamster.protobufparser.field.StringField;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmartParserImplTest extends AbstractParserTest {

    @Override
    protected Parser getParser() {
        return new SmartParserImpl(new FieldFactory());
    }

    @Test
    public void testObjectSmart() throws IOException {
        final List<Field<?>> fields = parse(data(
                0x12, 0x08, 0x0A, 0x02, 0x01, 0x00, 0x12, 0x02, 0x65, 0x73,
                0x18, 0x96, 0x01,
                0x0A, 0x06, 0x08, 0x98, 0x01, 0x12, 0x01, 0x69,
                0x12, 0x04, 0x12, 0x02, 0x00, 0x01
        ), Collections.emptyList());

        Assert.assertEquals(4, fields.size());

        // tag 2
        Assert.assertEquals(DataType.OBJECT, fields.get(0).getDataType());
        final List<Field<?>> fieldsTag2 = ((ObjectField) fields.get(0)).getValue();
        Assert.assertEquals(2, fieldsTag2.size());
        Assert.assertEquals(DataType.PACKED_SIGNED_INT, fieldsTag2.get(0).getDataType());
        Assert.assertEquals(Arrays.asList(-1L, 0L), ((PackedSignedIntField) fieldsTag2.get(0)).getValue());
        Assert.assertEquals(DataType.PACKED_SIGNED_INT, fieldsTag2.get(1).getDataType());
        Assert.assertEquals(Arrays.asList(-51L, -58L), ((PackedSignedIntField) fieldsTag2.get(1)).getValue());

        // tag 3
        Assert.assertEquals(DataType.SIGNED_INT, fields.get(1).getDataType());
        Assert.assertEquals(75L, (long) ((SignedIntField) fields.get(1)).getValue());

        // tag 1
        Assert.assertEquals(DataType.OBJECT, fields.get(2).getDataType());
        final List<Field<?>> fieldsTag1 = ((ObjectField) fields.get(2)).getValue();
        Assert.assertEquals(2, fieldsTag1.size());
        Assert.assertEquals(DataType.SIGNED_INT, fieldsTag1.get(0).getDataType());
        Assert.assertEquals(76L, (long) ((SignedIntField) fieldsTag1.get(0)).getValue());
        Assert.assertEquals(DataType.STRING, fieldsTag1.get(1).getDataType());
        Assert.assertEquals("i", ((StringField) fieldsTag1.get(1)).getValue());

        // tag 2
        Assert.assertEquals(DataType.OBJECT, fields.get(3).getDataType());
        final List<Field<?>> fieldsTag2Another = ((ObjectField) fields.get(3)).getValue();
        Assert.assertEquals(1, fieldsTag2Another.size());
        Assert.assertEquals(DataType.PACKED_SIGNED_INT, fieldsTag2Another.get(0).getDataType());
        Assert.assertEquals(Arrays.asList(0L, -1L), ((PackedSignedIntField) fieldsTag2Another.get(0)).getValue());
    }

}
