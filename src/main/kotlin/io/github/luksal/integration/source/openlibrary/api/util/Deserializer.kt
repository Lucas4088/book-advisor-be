package io.github.luksal.integration.source.openlibrary.api.util

import io.github.luksal.integration.source.openlibrary.api.dto.TypeRef
import io.github.luksal.integration.source.openlibrary.api.dto.TypeValue
import tools.jackson.core.JsonParser
import tools.jackson.databind.deser.std.StdDeserializer

class TypeValueDeserializer : StdDeserializer<TypeValue>(TypeValue::class.java) {
    override fun deserialize(p: JsonParser, ctxt: tools.jackson.databind.DeserializationContext): TypeValue =
        p.objectReadContext().readTree<tools.jackson.databind.JsonNode>(p).let {
            TypeValue(it.get("type")?.asString(), it.get("value")?.asString())
        }
}

class TypeRefDeserializer : StdDeserializer<TypeRef>(TypeRef::class.java) {
    override fun deserialize(p: JsonParser, ctxt: tools.jackson.databind.DeserializationContext): TypeRef=
        p.objectReadContext().readTree<tools.jackson.databind.JsonNode>(p).let {
            TypeRef(it.get("key")?.asString())
        }
}