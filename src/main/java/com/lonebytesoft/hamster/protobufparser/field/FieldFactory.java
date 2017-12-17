package com.lonebytesoft.hamster.protobufparser.field;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.Reader;
import com.lonebytesoft.hamster.protobufparser.WireType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FieldFactory {

    private static final Map<DataType, Definition> PACKED_FIELD_MEMBER = Arrays.stream(DataType.values())
            .filter(dataType -> dataType.getPackedMemberDataType() != null)
            .collect(Collectors.toMap(
                    Function.identity(),
                    dataType -> new Definition(null, -1, dataType.getPackedMemberDataType())
            ));

    public Field<?> buildField(final Definition definition, final InputStream input) throws IOException {
        final String name = definition.getName();
        final DataType dataType = definition.getDataType();

        switch(dataType) {
            case DOUBLE:
                final double valueDouble = Double.longBitsToDouble(Reader.convertToNumber(dataType.getWireType().readData(input)));
                return new DoubleField(name, valueDouble);

            case FLOAT:
                final float valueFloat = Float.intBitsToFloat((int) Reader.convertToNumber(dataType.getWireType().readData(input)));
                return new FloatField(name, valueFloat);

            case INT:
                final long valueInt = Reader.convertToNumber(dataType.getWireType().readData(input));
                return new IntField(name, valueInt);

            case SIGNED_INT:
                final long valueSignedInt = Reader.convertToNumber(dataType.getWireType().readData(input));
                // decoding ZigZag
                return new SignedIntField(name, (valueSignedInt >> 1) * (1 - 2 * (valueSignedInt & 1)));

            case FIXED_32:
                final int valueFixed32 = (int) Reader.convertToNumber(dataType.getWireType().readData(input));
                return new Fixed32Field(name, valueFixed32);

            case FIXED_64:
                final long valueFixed64 = Reader.convertToNumber(dataType.getWireType().readData(input));
                return new Fixed64Field(name, valueFixed64);

            case BOOL:
                final boolean valueBool = Reader.convertToNumber(dataType.getWireType().readData(input)) == 1;
                return new BoolField(name, valueBool);

            case STRING:
                final String valueString = new String(dataType.getWireType().readData(input));
                return new StringField(name, valueString);

            case BYTES:
                final byte[] valueBytes = dataType.getWireType().readData(input);
                return new BytesField(name, valueBytes);

            case OBJECT:
                final byte[] data = dataType.getWireType().readData(input);
                return new BytesField(name, data);

            case PACKED_INT:
                final List<Long> valuePackedInt = readPacked(PACKED_FIELD_MEMBER.get(dataType), input);
                return new PackedIntField(name, valuePackedInt);

            case PACKED_SIGNED_INT:
                final List<Long> valuePackedSignedInt = readPacked(PACKED_FIELD_MEMBER.get(dataType), input);
                return new PackedSignedIntField(name, valuePackedSignedInt);

            case PACKED_BOOL:
                final List<Boolean> valuePackedBool = readPacked(PACKED_FIELD_MEMBER.get(dataType), input);
                return new PackedBoolField(name, valuePackedBool);

            default:
                throw new IllegalStateException("Can't build field for " + dataType);
        }
    }

    private <T> List<T> readPacked(final Definition definitionGeneric, final InputStream input) throws IOException {
        final byte[] data = WireType.VAR_LENGTH.readData(input);
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
