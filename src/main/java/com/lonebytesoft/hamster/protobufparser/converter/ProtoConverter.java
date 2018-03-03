package com.lonebytesoft.hamster.protobufparser.converter;

import com.lonebytesoft.hamster.protobufparser.DataType;
import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.field.Field;
import com.lonebytesoft.hamster.protobufparser.field.ObjectField;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtoConverter {

    private static final String PROTOBUF_INDENT = "  ";
    private static final String PROTOBUF_TYPE_PREFIX = "type_";

    public String toProto(final String rootName, final Collection<Definition> definitions) {
        final Collection<ProtobufDefinition> protobufDefinitions = definitionsToProtobufDefinitions(definitions);
        return toProtoInternal(rootName, protobufDefinitions);
    }

    public String toProto(final String rootName, final Collection<Definition> definitions,
                          final Collection<Field<?>> fields, final boolean includeGeneric) {
        final Collection<ProtobufDefinition> definitionsProtobufDefinitions = definitionsToProtobufDefinitions(definitions);
        final Collection<ProtobufDefinition> fieldsProtobufDefinitions = fieldsToProtobufDefinitions(fields);

        final Collection<ProtobufDefinition> protobufDefinitions = includeGeneric
                ? merge(definitionsProtobufDefinitions, fieldsProtobufDefinitions, false)
                : modify(definitionsProtobufDefinitions, fieldsProtobufDefinitions);

        return toProtoInternal(rootName, protobufDefinitions);
    }

    private Collection<ProtobufDefinition> definitionsToProtobufDefinitions(final Collection<Definition> definitions) {
        return definitions.stream()
                .map(definition -> {
                    if(isCompound(definition.getDataType())) {
                        return new ProtobufDefinition(true, definition.getDataType().getType(), definition.getName(), definition.getTag(),
                                definitionsToProtobufDefinitions(definition.getChildren().values()));
                    } else {
                        return new ProtobufDefinition(true, definition.getDataType().getType(), definition.getName(), definition.getTag());
                    }
                })
                .collect(Collectors.toList());
    }

    private Collection<ProtobufDefinition> fieldsToProtobufDefinitions(final Collection<Field<?>> fields) {
        final Map<Long, ProtobufDefinition> definitions = new HashMap<>();

        fields.forEach(field -> {
            definitions.compute(field.getTag(), (tag, definition) -> {
                if(definition == null) {
                    if(isCompound(field.getDataType())) {
                        return new ProtobufDefinition(false, field.getDataType().getType(), buildGenericName(field), field.getTag(),
                                fieldsToProtobufDefinitions(((ObjectField) field).getValue()));
                    } else {
                        return new ProtobufDefinition(false, field.getDataType().getType(), buildGenericName(field), field.getTag());
                    }
                } else {
                    validateDefinitions(definition, field);

                    if(isCompound(field.getDataType())) {
                        return new ProtobufDefinition(true, definition.getType(), definition.getName(), definition.getTag(),
                                merge(definition.getChildren(), fieldsToProtobufDefinitions(((ObjectField) field).getValue()), true));
                    } else {
                        return new ProtobufDefinition(true, definition.getType(), definition.getName(), definition.getTag());
                    }
                }
            });
        });

        return definitions.values();
    }

    private Collection<ProtobufDefinition> merge(final Collection<ProtobufDefinition> first, final Collection<ProtobufDefinition> second,
                                                 final boolean considerFirstRepeated) {
        final Map<Long, ProtobufDefinition> definitions = first.stream()
                .collect(Collectors.toMap(ProtobufDefinition::getTag, Function.identity()));
        
        second.forEach(definition -> {
            definitions.compute(definition.getTag(), (tag, definitionFirst) -> {
                if(definitionFirst == null) {
                    return definition;
                } else {
                    validateDefinitions(definition, definitionFirst);

                    final boolean isRepeated = definition.isRepeated() || considerFirstRepeated && definitionFirst.isRepeated();
                    if(definition.isCompound()) {
                        return new ProtobufDefinition(isRepeated, definitionFirst.getType(), definitionFirst.getName(), definitionFirst.getTag(),
                                merge(definitionFirst.getChildren(), definition.getChildren(), considerFirstRepeated));
                    } else {
                        return new ProtobufDefinition(isRepeated, definitionFirst.getType(), definitionFirst.getName(), definitionFirst.getTag());
                    }
                }
            });
        });

        return definitions.values();
    }

    private Collection<ProtobufDefinition> modify(final Collection<ProtobufDefinition> target, final Collection<ProtobufDefinition> reference) {
        final Map<Long, ProtobufDefinition> referenceDefinitions = reference.stream()
                .collect(Collectors.toMap(ProtobufDefinition::getTag, Function.identity()));

        return target.stream()
                .map(definition -> {
                    if(referenceDefinitions.containsKey(definition.getTag())) {
                        final ProtobufDefinition referenceDefinition = referenceDefinitions.get(definition.getTag());
                        validateDefinitions(definition, referenceDefinition);

                        if(definition.isCompound()) {
                            return new ProtobufDefinition(referenceDefinition.isRepeated(), definition.getType(),
                                    definition.getName(), definition.getTag(), modify(definition.getChildren(), referenceDefinition.getChildren()));
                        } else {
                            return new ProtobufDefinition(referenceDefinition.isRepeated(), definition.getType(),
                                    definition.getName(), definition.getTag());
                        }
                    } else {
                        return definition;
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean isCompound(final DataType dataType) {
        return dataType == DataType.OBJECT;
    }

    private void validateDefinitions(final ProtobufDefinition definition, final Field<?> field) {
        if(!definition.getType().equals(field.getDataType().getType())) {
            throw new IllegalArgumentException(String.format("Incompatible field types with the same tag #%d: %s and %s",
                    field.getTag(), definition.getType(), field.getDataType().getType()));
        }
    }

    private void validateDefinitions(final ProtobufDefinition first, final ProtobufDefinition second) {
        if(!first.getType().equals(second.getType())) {
            throw new IllegalArgumentException(String.format("Incompatible definition types with the same tag #%d: %s and %s",
                    first.getTag(), first.getType(), second.getType()));
        }
    }

    private String toProtoInternal(final String rootName, final Collection<ProtobufDefinition> definitions) {
        final StringBuilder result = new StringBuilder();

        result.append("syntax = \"proto3\";\n");
        result
                .append(DataType.OBJECT.getType())
                .append(" ")
                .append(normalizeProtobufIdentifier(rootName))
                .append(" {\n");

        definitions.forEach(definition -> appendDefinition(definition, result, PROTOBUF_INDENT));

        result.append("}\n");

        return result.toString();
    }

    private void appendDefinition(final ProtobufDefinition definition, final StringBuilder result, final String indent) {
        final String name = normalizeProtobufIdentifier(definition.getName());

        final String typeName;
        if(definition.isCompound()) {
            typeName = PROTOBUF_TYPE_PREFIX + name;

            result
                    .append(indent)
                    .append(DataType.OBJECT.getType())
                    .append(" ")
                    .append(typeName)
                    .append(" {\n");
            definition.getChildren().forEach(
                    child -> appendDefinition(child, result, indent + PROTOBUF_INDENT)
            );
            result
                    .append(indent)
                    .append("}\n");
        } else {
            typeName = definition.getType();
        }

        result
                .append(indent)
                .append(definition.isRepeated() ? "repeated " : "")
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

    private String buildGenericName(final Field<?> field) {
        return "<#" + field.getTag() + " " + field.getDataType().getWireType() + ">";
    }

}
