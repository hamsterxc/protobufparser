package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.DataType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class PossibleDataTypes {

    private final Map<DataType, Boolean> possibleDataTypes;

    public PossibleDataTypes(final Function<DataType, Boolean> producer) {
        possibleDataTypes = GenericVarLenField.DATA_TYPES.stream()
                .collect(
                        () -> new EnumMap<>(DataType.class),
                        (map, dataType) -> map.put(dataType, producer.apply(dataType)),
                        EnumMap::putAll
                );
    }

    public DataType getFirstPossibleDataType() {
        return GenericVarLenField.DATA_TYPES.stream()
                .filter(possibleDataTypes::get)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No possible data types found"));
    }

    public PossibleDataTypes merge(final PossibleDataTypes another) {
        if(another == null) {
            return this;
        }

        return new PossibleDataTypes(dataType -> possibleDataTypes.get(dataType) && another.possibleDataTypes.get(dataType));
    }

}
