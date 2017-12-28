package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

public class FieldFactory {

    private static final ThreadLocal<CharsetDecoder> STRING_DECODER = ThreadLocal.withInitial(() ->
            Charset.forName("UTF-8").newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
    );

    public Field<?> buildField(final Definition definition, final InputStream input) throws IOException {
        return buildField(definition, definition.getDataType().getWireType().readData(input));
    }
    
    public Field<?> buildField(final Definition definition, final byte[] data) throws IOException {
        final String name = definition.getName();
        final DataType dataType = definition.getDataType();

        switch(dataType) {
            case DOUBLE:
                final double valueDouble = Double.longBitsToDouble(Reader.convertToNumber(data));
                return new DoubleField(name, valueDouble);

            case FLOAT:
                final float valueFloat = Float.intBitsToFloat((int) Reader.convertToNumber(data));
                return new FloatField(name, valueFloat);

            case INT:
                final long valueInt = Reader.convertToNumber(data);
                return new IntField(name, valueInt);

            case SIGNED_INT:
                final long valueSignedInt = Reader.convertToNumber(data);
                // decoding ZigZag
                return new SignedIntField(name, (valueSignedInt >> 1) * (1 - 2 * (valueSignedInt & 1)));

            case FIXED_32:
                final int valueFixed32 = (int) Reader.convertToNumber(data);
                return new Fixed32Field(name, valueFixed32);

            case FIXED_64:
                final long valueFixed64 = Reader.convertToNumber(data);
                return new Fixed64Field(name, valueFixed64);

            case BOOL:
                final boolean valueBool = Reader.convertToNumber(data) == 1;
                return new BoolField(name, valueBool);

            case STRING:
                final String valueString;
                try {
                     valueString = STRING_DECODER.get()
                            .decode(ByteBuffer.wrap(data))
                            .toString();
                } catch (Exception e) {
                    throw new IOException(e);
                }

                return new StringField(name, valueString);

            case BYTES:
                return new BytesField(name, data);

            case OBJECT:
                return new BytesField(name, data);

            case PACKED_INT:
                final List<Long> valuePackedInt = readPacked(dataType.getPackedMemberDataType(), data);
                return new PackedIntField(name, valuePackedInt);

            case PACKED_SIGNED_INT:
                final List<Long> valuePackedSignedInt = readPacked(dataType.getPackedMemberDataType(), data);
                return new PackedSignedIntField(name, valuePackedSignedInt);

            case PACKED_BOOL:
                final List<Boolean> valuePackedBool = readPacked(dataType.getPackedMemberDataType(), data);
                return new PackedBoolField(name, valuePackedBool);

            default:
                throw new IllegalStateException("Can't build field for " + dataType);
        }
    }

    private <T> List<T> readPacked(final DataType memberType, final byte[] data) throws IOException {
        final Definition definitionGeneric = new Definition(null, -1, memberType);
        final PushbackInputStream subInput = new PushbackInputStream(new ByteArrayInputStream(data));

        final List<T> result = new ArrayList<>();
        while(Reader.hasMore(subInput)) {
            @SuppressWarnings("unchecked")
            final Field<T> fieldGeneric = (Field<T>) buildField(definitionGeneric, subInput);

            result.add(fieldGeneric.getValue());
        }
        return result;
    }

}
