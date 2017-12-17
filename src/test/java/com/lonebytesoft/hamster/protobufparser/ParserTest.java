package com.lonebytesoft.hamster.protobufparser;

import com.lonebytesoft.hamster.protobufparser.field.BytesField;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.FieldFactory;
import com.lonebytesoft.hamster.protobufparser.field.IntField;
import com.lonebytesoft.hamster.protobufparser.field.ObjectField;
import com.lonebytesoft.hamster.protobufparser.field.PackedBoolField;
import com.lonebytesoft.hamster.protobufparser.field.PackedIntField;
import com.lonebytesoft.hamster.protobufparser.field.SignedIntField;
import com.lonebytesoft.hamster.protobufparser.field.StringField;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ParserTest {

    private final Parser parser = new Parser(new FieldFactory());

    @Test
    public void testInt() throws IOException {
        final List<Field<?>> fields = parse(data(0x08, 0x97, 0x01), Arrays.asList(
                new Definition(null, 1, DataType.INT)
        ));

        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(DataType.INT, fields.get(0).getDataType());
        Assert.assertEquals(151L, (long) ((IntField) fields.get(0)).getValue());
    }

    @Test
    public void testSignedInt() throws IOException {
        final List<Field<?>> fields = parse(data(0x08, 0x97, 0x01), Arrays.asList(
                new Definition(null, 1, DataType.SIGNED_INT)
        ));

        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(DataType.SIGNED_INT, fields.get(0).getDataType());
        Assert.assertEquals(-75L, (long) ((SignedIntField) fields.get(0)).getValue());
    }

    @Test
    public void testPackedInt() throws IOException {
        final List<Field<?>> fields = parse(data(0xAA, 0x08, 0x05, 0x97, 0x01, 0x01, 0x98, 0x01), Arrays.asList(
                new Definition(null, 133, DataType.PACKED_INT)
        ));

        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(DataType.PACKED_INT, fields.get(0).getDataType());
        Assert.assertEquals(Arrays.asList(151L, 1L, 152L), ((PackedIntField) fields.get(0)).getValue());
    }

    @Test
    public void testBool() throws IOException {
        final List<Field<?>> fields = parse(data(0x0A, 0x02, 0x00, 0x01), Arrays.asList(
                new Definition(null, 1, DataType.PACKED_BOOL)
        ));

        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(DataType.PACKED_BOOL, fields.get(0).getDataType());
        Assert.assertEquals(Arrays.asList(false, true), ((PackedBoolField) fields.get(0)).getValue());
    }

    @Test
    public void testString() throws IOException {
        final List<Field<?>> fields = parse(data(0x0A, 0x07, 0x74, 0x65, 0x73, 0x74, 0x69, 0x6e, 0x67), Arrays.asList(
                new Definition(null, 1, DataType.STRING)
        ));

        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(DataType.STRING, fields.get(0).getDataType());
        Assert.assertEquals("testing", ((StringField) fields.get(0)).getValue());
    }

    @Test
    public void testObject() throws IOException {
        final List<Field<?>> fields = parse(data(
                0x12, 0x08, 0x0A, 0x02, 0x01, 0x00, 0x12, 0x02, 0x65, 0x73,
                0x18, 0x96, 0x01,
                0x0A, 0x06, 0x08, 0x98, 0x01, 0x12, 0x01, 0x69
        ), Arrays.asList(
                new Definition(null, 1, DataType.OBJECT, Arrays.asList(
                        new Definition(null, 2, DataType.STRING)
                )),
                new Definition(null, 2, DataType.OBJECT, Arrays.asList(
                        new Definition(null, 1, DataType.PACKED_BOOL)
                )),
                new Definition(null, 3, DataType.INT)
        ));

        Assert.assertEquals(3, fields.size());

        Assert.assertEquals(DataType.OBJECT, fields.get(0).getDataType());
        final List<Field<?>> fieldsTag2 = ((ObjectField) fields.get(0)).getValue();
        Assert.assertEquals(2, fieldsTag2.size());
        Assert.assertEquals(DataType.PACKED_BOOL, fieldsTag2.get(0).getDataType());
        Assert.assertEquals(Arrays.asList(true, false), ((PackedBoolField) fieldsTag2.get(0)).getValue());
        Assert.assertEquals(DataType.BYTES, fieldsTag2.get(1).getDataType());
        Assert.assertArrayEquals(new byte[]{0x65, 0x73}, ((BytesField) fieldsTag2.get(1)).getValue());

        Assert.assertEquals(DataType.INT, fields.get(1).getDataType());
        Assert.assertEquals(150L, (long) ((IntField) fields.get(1)).getValue());

        Assert.assertEquals(DataType.OBJECT, fields.get(2).getDataType());
        final List<Field<?>> fieldsTag1 = ((ObjectField) fields.get(2)).getValue();
        Assert.assertEquals(2, fieldsTag1.size());
        Assert.assertEquals(DataType.SIGNED_INT, fieldsTag1.get(0).getDataType());
        Assert.assertEquals(76L, (long) ((SignedIntField) fieldsTag1.get(0)).getValue());
        Assert.assertEquals(DataType.STRING, fieldsTag1.get(1).getDataType());
        Assert.assertEquals("i", ((StringField) fieldsTag1.get(1)).getValue());
    }

    @Test(expected = IOException.class)
    public void testInsufficientData() throws IOException {
        parse(data(0x0A, 0x02, 0x00), Collections.emptyList());
    }

    private byte[] data(final int... bytes) {
        final byte[] data = new byte[bytes.length];
        IntStream.range(0, bytes.length).forEach(index -> data[index] = (byte) bytes[index]);
        return data;
    }

    private List<Field<?>> parse(final byte[] data, final Collection<Definition> definitions) throws IOException {
        return parser.parse(new ByteArrayInputStream(data), definitions);
    }

}
