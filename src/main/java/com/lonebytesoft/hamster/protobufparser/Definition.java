package com.lonebytesoft.hamster.protobufparser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Definition {

    private final String name;
    private final long tag;
    private final DataType dataType;
    private final Map<Long, Definition> children;

    public Definition(final String name, final long tag, final DataType dataType) {
        this(name, tag, dataType, Collections.emptyList());
    }

    public Definition(final String name, final long tag, final DataType dataType, final Collection<Definition> children) {
        this.name = name;
        this.tag = tag;
        this.dataType = dataType;
        this.children = Collections.unmodifiableMap(children.stream()
                .collect(Collectors.toMap(Definition::getTag, Function.identity())));
    }

    public String getName() {
        return name;
    }

    public long getTag() {
        return tag;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Map<Long, Definition> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "name='" + name + '\'' +
                ", tag=" + tag +
                ", dataType=" + dataType +
                '}';
    }

}
