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
    override fun deserialize(p: JsonParser, ctx: DeserializationContext): LocalDateTime? {
        val node: JsonNode = p.readValueAsTree()
        return when {
            node.isString -> parseDate(node.asString())
            node.isObject && node.has("\$date") -> {
                val date = node["\$date"]
                when {
                    date.isString -> parseDate(date.asString())
                    date.isNumber  -> LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(date.asLong()), ZoneOffset.UTC
                    )
                    else -> null
                }
            }
            node.isNull -> null
            else -> null
        }
    }

    private fun parseDate(text: String): LocalDateTime =
        Instant.parse(text).atOffset(ZoneOffset.UTC).toLocalDateTime()
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