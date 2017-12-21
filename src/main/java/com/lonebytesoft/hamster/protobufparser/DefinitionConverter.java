package com.lonebytesoft.hamster.protobufparser;

import java.util.Collection;

public class DefinitionConverter {

    private static final String PROTOBUF_INDENT = "  ";
    private static final String PROTOBUF_TYPE_PREFIX = "type_";

    public String toProtobuf(final Collection<Definition> definitions, final String rootName) {
        final StringBuilder result = new StringBuilder();

        result.append("syntax = \"proto3\";\n");
        result
                .append(DataType.OBJECT.getType())
                .append(" ")
                .append(normalizeProtobufIdentifier(rootName))
                .append(" {\n");

        definitions.forEach(definition -> appendDefinitionProtobuf(definition, result, PROTOBUF_INDENT));

        result.append("}\n");

        return result.toString();
    }

    private void appendDefinitionProtobuf(final Definition definition, final StringBuilder result, final String indent) {
        final String name = normalizeProtobufIdentifier(definition.getName());

        final String typeName;
        if(definition.getDataType() == DataType.OBJECT) {
            typeName = PROTOBUF_TYPE_PREFIX + name;

            result
                    .append(indent)
                    .append(DataType.OBJECT.getType())
                    .append(" ")
                    .append(typeName)
                    .append(" {\n");
            definition.getChildren().values().forEach(
                    child -> appendDefinitionProtobuf(child, result, indent + PROTOBUF_INDENT)
            );
            result
                    .append(indent)
                    .append("}\n");
        } else {
            typeName = definition.getDataType().getType();
        }

        result
                .append(indent)
                .append("repeated ")
                .append(typeName)
                .append(" ")
                .append(name)
                .append(" = ")
                .append(definition.getTag())
                .append(";\n");
    }

    private String normalizeProtobufIdentifier(final String raw) {
        final int underscore = "_".codePointAt(0);
        final String identifier = raw.codePoints()
                .map(c -> Character.isJavaIdentifierPart(c) ? c : underscore)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        if(!Character.isJavaIdentifierStart(identifier.codePointAt(0))) {
            return "_" + identifier;
        } else {
            return identifier;
        }
    }

}
