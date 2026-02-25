package io.github.luksal.integration.source.openlibrary.api.dto

import io.github.luksal.integration.source.openlibrary.api.util.TypeRefDeserializer
import io.github.luksal.integration.source.openlibrary.api.util.TypeValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

data class OpenLibraryBookDetails(
    @JsonDeserialize(using = TypeValueDeserializer::class)
    val description: TypeValue? = null,
    val links: List<Link>? = emptyList(),
    val title: String,
    val firstPublishYear: String?, //"April 11, 1947"
    val covers: List<Long>? = emptyList(),
    val numberOfPages: Int? = null,
    val subjectPlaces: List<String>? = emptyList(),
    val subjectPeople: List<String>? = emptyList(),
    val key: String,
    val authors: List<AuthorRole>? = emptyList(),
    val subjects: List<String>? = emptyList(),
    val subjectTimes: List<String>? = emptyList(),
    val latestRevision: Int? = null,
    val revision: Int? = null,
    val created: DateValue? = null,
    val lastModified: DateValue? = null
) {
    override fun toString(): String =
        buildString {
            appendLine("Title: ${title ?: "N/A"}")
            appendLine()

            appendLine("Description:")
            appendLine(description ?: "N/A")
            appendLine()

            appendLine("Authors:")
            authors?.forEach {
                appendLine("- ${it.author?.key}")
            } ?: appendLine("N/A")
            appendLine()

            appendLine("Subjects:")
            subjects?.forEach {
                appendLine("- $it")
            } ?: appendLine("N/A")
        }
}

data class Link(
    val title: String?,
    val url: String?,
    val type: TypeRef?
)

data class AuthorRole(
    val author: KeyRef?,
    @JsonDeserialize(using = TypeRefDeserializer::class)
    val type: TypeRef?
)

data class Excerpt(
    val excerpt: String?,
    val comment: String? = null,
    val author: KeyRef? = null
)

data class KeyRef(
    val key: String?
)

data class TypeRef(
    val key: String?
)

data class DateValue(
    val type: String?,
    val value: String?
)

data class TypeValue(
    val type: String?,
    val value: String?
)

