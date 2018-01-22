package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.field.Field;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class GenericVarLenField extends Field<Void> {
    
    public static final List<DataType> DATA_TYPES = Collections.unmodifiableList(Arrays.asList(
            DataType.OBJECT,
            DataType.STRING,
            DataType.PACKED_SIGNED_INT,
            DataType.BYTES
    ));
    
    private final Map<DataType, Field<?>> fields;

    public GenericVarLenField(final long tag) {
        super(tag);

        fields = DATA_TYPES.stream()
                .collect(
                        () -> new EnumMap<>(DataType.class),
                        (map, dataType) -> map.put(dataType, null),
                        EnumMap::putAll
                );
    }

    @Override
    public DataType getDataType() {
        return null;
    }

    @Override
    public Void getValue() {
        return null;
    }

    public Field<?> getField(final DataType dataType) {
        if(DATA_TYPES.contains(dataType)) {
            return fields.get(dataType);
        } else {
            throw new IllegalArgumentException("Data type " + dataType
                        + " not supported by " + getClass().getSimpleName());
        }
    }

    public void setField(final Field<?> field) {
        final DataType dataType = field.getDataType();
        if(DATA_TYPES.contains(dataType)) {
            fields.put(dataType, field);
        } else {
            throw new IllegalArgumentException("Data type " + dataType
                    + " not supported by " + getClass().getSimpleName());
        }
    }

    public PossibleDataTypes calculatePossibleDataTypes() {
        return new PossibleDataTypes(dataType -> fields.get(dataType) != null);
    }

}
