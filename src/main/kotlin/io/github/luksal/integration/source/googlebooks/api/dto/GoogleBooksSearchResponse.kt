package io.github.luksal.integration.source.googlebooks.api.dto

import io.github.luksal.book.model.Author
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.BookEdition
import io.github.luksal.book.model.Genre
import java.time.Year
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class GoogleBooksSearchResponse(
    val kind: String?,
    val totalItems: Int,
    val items: List<BookItem>?
)

data class BookItem(
    val kind: String,
    val id: String,
    val etag: String,
    val volumeInfo: VolumeInfo,
    val accessInfo: AccessInfo,
    val searchInfo: SearchInfo?
)

data class VolumeInfo(
    val title: String,
    val subtitle: String? = null,
    val authors: List<String>?,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val industryIdentifiers: List<IndustryIdentifier>?,
    val readingModes: ReadingModes,
    val pageCount: Int? = null,
    val printType: String? = null,
    val categories: List<String>? = null,
    val maturityRating: String? = null,
    val allowAnonLogging: Boolean? = null,
    val contentVersion: String? = null,
    val panelizationSummary: PanelizationSummary? = null,
    val imageLinks: ImageLinks? = null,
    val language: String? = null,
    val previewLink: String? = null,
    val infoLink: String? = null,
    val canonicalVolumeLink: String? = null
)

data class IndustryIdentifier(
    val type: String,
    val identifier: String
)

data class ReadingModes(
    val text: Boolean,
    val image: Boolean
)

data class PanelizationSummary(
    val containsEpubBubbles: Boolean,
    val containsImageBubbles: Boolean
)

data class ImageLinks(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null
)

data class Price(
    val amount: Double,
    val currencyCode: String
)

data class Offer(
    val finskyOfferType: Int,
    val listPrice: PriceMicros,
    val retailPrice: PriceMicros
)

data class PriceMicros(
    val amountInMicros: Long,
    val currencyCode: String
)

data class AccessInfo(
    val country: String,
    val viewability: String,
    val embeddable: Boolean,
    val publicDomain: Boolean,
    val textToSpeechPermission: String,
    val epub: FormatAvailability,
    val pdf: FormatAvailability,
    val webReaderLink: String,
    val accessViewStatus: String,
    val quoteSharingAllowed: Boolean
)

data class FormatAvailability(
    val isAvailable: Boolean,
    val acsTokenLink: String? = null,
    val downloadLink: String? = null
)

data class SearchInfo(
    val textSnippet: String
)
