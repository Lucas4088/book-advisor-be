package io.github.luksal.book.db.util

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.deser.std.StdDeserializer
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class MongoDateDeserializer : StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        val node = p.readValueAsTree<JsonNode>()
        val millis = node["\$date"].asLong()
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
    }


}

class MongoBigDecimalDeserializer : StdDeserializer<BigDecimal>(BigDecimal::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
        val node: JsonNode = p.readValueAsTree()
        return when {
            node.isObject -> BigDecimal(node["\$numberDecimal"].asString())
            node.isNumber -> node.decimalValue()
            else -> BigDecimal(node.asText())
        }
    }
}