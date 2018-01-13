package com.lonebytesoft.hamster.protobufparser.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class ProtobufDefinition {

    private final boolean isRepeated;
    private final String type;
    private final String name;
    private final long tag;
    private final boolean isCompound;
    private final Collection<ProtobufDefinition> children;

    public ProtobufDefinition(final boolean isRepeated, final String type, final String name, final long tag) {
        this.isRepeated = isRepeated;
        this.type = type;
        this.name = name;
        this.tag = tag;
        this.isCompound = false;
        this.children = Collections.emptyList();
    }

    public ProtobufDefinition(final boolean isRepeated, final String type, final String name, final long tag,
                              final Collection<ProtobufDefinition> children) {
        this.isRepeated = isRepeated;
        this.type = type;
        this.name = name;
        this.tag = tag;
        this.isCompound = true;
        this.children = Collections.unmodifiableCollection(new ArrayList<>(children));
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public long getTag() {
        return tag;
    }

    public boolean isCompound() {
        return isCompound;
    }

    public Collection<ProtobufDefinition> getChildren() {
        return children;
    }

}
