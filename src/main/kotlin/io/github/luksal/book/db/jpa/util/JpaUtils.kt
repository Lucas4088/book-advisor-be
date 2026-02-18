package io.github.luksal.book.db.jpa.util

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListToStringConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String =
        attribute?.joinToString(";") ?: ""

    override fun convertToEntityAttribute(dbData: String?): List<String> =
        dbData?.takeIf { it.isNotBlank() }?.split(";") ?: emptyList()
}
