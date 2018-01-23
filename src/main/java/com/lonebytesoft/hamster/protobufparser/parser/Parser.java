package com.lonebytesoft.hamster.protobufparser.parser;

import com.lonebytesoft.hamster.protobufparser.Definition;
import com.lonebytesoft.hamster.protobufparser.field.Field;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface Parser {

    List<Field<?>> parse(InputStream input, Collection<Definition> definitions) throws IOException;

}
