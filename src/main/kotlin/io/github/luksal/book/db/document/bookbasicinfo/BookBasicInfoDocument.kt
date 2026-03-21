package io.github.luksal.book.db.document.bookbasicinfo

import io.github.luksal.util.ext.murmurHash3
import io.github.luksal.util.ext.normalizeWhiteChars
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "book-basic-info")
class BookBasicInfoDocument(
    @Id
    val id: String ? = null,
    @Indexed(unique = true)
    @Field(name = "pbi")
    val bookPublicId: String,
    @TextIndexed
    @Field(name = "tt")
    val title: String,
    @Field(name = "olk")
    val openLibraryKey: String? = null,
    @Field(name = "olek")
    val openLibraryEditionKey: String? = null,
    @Field("et")
    val editionTitle: String = title,
    @Field(name = "fby")
    val firstPublishYear: Int? = null,
    @Field(name = "fpd")
    val firstPublishDate: String? = null,
    @Field(name = "au")
    val authors: List<String> = emptyList(),
    @Field(name = "ak")
    val authorsKeys: List<String> = emptyList(),
    @Field(name = "la")
    val lang: String,
    @Field(name = "su")
    val subjects: List<String> = emptyList(),
    @Field(name = "des")
    val description: String?,
) {
    companion object {
        fun generatePublicId(title: String, authors: List<String>): String =
            ("$title|${authors.sorted().joinToString(",")}").normalizeWhiteChars().murmurHash3()
    }
}