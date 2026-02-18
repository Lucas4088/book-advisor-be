package io.github.luksal.book.mapper

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.book.*
import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.model.Author
import io.github.luksal.book.model.AuthorUpdate
import io.github.luksal.book.model.Book
import io.github.luksal.book.model.BookEdition
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.book.model.Genre
import io.github.luksal.book.model.GenreUpdate
import io.github.luksal.book.model.RatingUpdate
import io.github.luksal.integration.source.googlebooks.api.dto.BookItem
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryBookDetails
import java.time.Year
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object BookMapper {
    fun map(book: BookUpdate): BookDocument {
        return BookDocument(
            id = book.id,
            title = book.edition?.title ?: "",
            description = book.description ?: "",
            publishingYear = book.publishingYear?.value,
            pageCount = book.pageCount ?: 0,
            edition = mapEdition(book.edition),
            thumbnailUrl = book.thumbnailUrl ?: "",
            smallThumbnailUrl = book.smallThumbnailUrl ?: "",
            authors = mapAuthors(book.authors),
            genres = mapGenres(book.genres),
            ratings = mapRatings(book.ratings)
        )
    }

    fun mapEdition(edition: BookEdition?): EditionEmbedded? =
        edition?.let { EditionEmbedded(title = it.title, lang = it.lang) }

    fun mapAuthors(authors: List<AuthorUpdate>?): List<AuthorEmbedded> =
        authors?.map { AuthorEmbedded(publicId = it.publicId, name = it.name) } ?: emptyList()

    fun mapGenres(genres: List<GenreUpdate>?): List<GenreEmbedded> =
        genres?.map { GenreEmbedded(name = it.name) } ?: emptyList()

    fun mapRatings(ratings: List<RatingUpdate>?): Set<RatingEmbedded> =
        ratings?.map {
            RatingEmbedded(
                rating = it.score,
                count = it.count,
                source = RatingSourceEmbedded(
                    name = it.source.name,
                    url = it.source.url
                )
            )
        }?.toSet() ?: emptySet()

    fun map(book: Book): BookDocument {
        return BookDocument(
            id = book.id,
            title = book.title,
            description = book.description,
            publishingYear = book.publishingYear?.value,
            pageCount = book.pageCount,
            edition = book.edition?.let {
                EditionEmbedded(
                    title = it.title,
                    lang = it.lang
                )
            },
            thumbnailUrl = book.thumbnailUrl,
            smallThumbnailUrl = book.smallThumbnailUrl,
            authors = book.authors.map {
                AuthorEmbedded(
                    publicId = it.publicId,
                    name = it.name
                )
            },
            genres = book.genres.map {
                GenreEmbedded(
                    name = it.name
                )
            },
            ratings = book.ratings.map {
                RatingEmbedded(
                    rating = it.score,
                    count = it.count,
                    source = RatingSourceEmbedded(
                        name = it.source.name,
                        url = it.source.url
                    )
                )
            }.toSet()
        )
    }

    fun map(book: BookEntity): BookSearchResponse = BookSearchResponse(
        id = book.id!!,
        title = book.title,
        smallThumbnailUrl = book.smallThumbnailUrl
    )

    fun map(book: BookDocument) = BookSearchResponse(
        id = book.id,
        title = book.title,
        smallThumbnailUrl = book.smallThumbnailUrl
    )

    fun map(details: OpenLibraryBookDetails): BookUpdate = BookUpdate(
        id = details.key,
        description = details.description?.value,
       /* publishingYear = details.created?.value?.let { value ->
            try {
                java.time.Year.parse(value.take(4))
            } catch (e: Exception) {
                null
            }
        },*/
        //edition = details.title?.let { io.github.luksal.book.model.BookEdition(it, null) },
        //thumbnailUrl = details.covers?.firstOrNull()?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" },
        //smallThumbnailUrl = details.covers?.firstOrNull()?.let { "https://covers.openlibrary.org/b/id/$it-S.jpg" },
       /* authors = details.authors?.mapNotNull { authorRole ->
            authorRole.author?.key?.let { key ->
                io.github.luksal.book.model.Author(
                    id = null,
                    publicId = key,
                    name = "",
                    otherNames = null
                )
            }
        },*/
        genres = details.subjects?.map { subject ->
            GenreUpdate(
                id = null,
                name = subject
            )
        }
    )

    @OptIn(ExperimentalUuidApi::class)
    fun map(item: BookItem, publicId: String, editionTitle: String?, lang: String): Book = Book(
        id = publicId,
        title = item.volumeInfo.title,
        description = item.volumeInfo.description ?: "",
        publishingYear = item.volumeInfo.publishedDate?.take(4)?.toIntOrNull()?.let { Year.of(it) },
        pageCount = item.volumeInfo.pageCount ?: 0,
        edition = editionTitle?.let { BookEdition(it, lang) },
        thumbnailUrl = item.volumeInfo.imageLinks?.thumbnail ?: "",
        smallThumbnailUrl = item.volumeInfo.imageLinks?.smallThumbnail ?: "",
        authors = item.volumeInfo.authors?.mapIndexed { idx, name ->
            Author(
                id = idx.toLong(),
                publicId = Uuid.generateV7().toString(),
                name = name
            )
        } ?: emptyList(),
        genres = item.volumeInfo.categories?.mapIndexed { idx, name -> Genre(id = idx.toLong(), name = name) } ?: emptyList(),
        ratings = emptyList()
    )
}
