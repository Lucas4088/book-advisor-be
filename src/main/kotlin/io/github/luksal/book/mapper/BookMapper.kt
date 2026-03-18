package io.github.luksal.book.mapper

import io.github.luksal.book.api.dto.*
import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.book.*
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.jpa.model.*
import io.github.luksal.book.model.*
import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveSearchDoc
import io.github.luksal.integration.source.googlebooks.api.dto.BookItem
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryBookDetails
import io.github.luksal.integration.source.openlibrary.api.dto.OpenLibraryDoc
import java.time.LocalDateTime
import java.time.Year
import kotlin.uuid.ExperimentalUuidApi

object BookMapper {
    fun mapEdition(edition: BookEdition?): EditionEmbedded? =
        edition?.let { EditionEmbedded(title = it.title, lang = it.lang) }

    fun mapAuthors(authors: List<AuthorUpdate>?): List<AuthorEmbedded> =
        authors?.map { AuthorEmbedded(name = it.name, key = it.key) } ?: emptyList()

    fun mapGenres(genres: List<GenreUpdate>?): List<GenreEmbedded> =
        genres?.map { GenreEmbedded(name = it.name) } ?: emptyList()

    fun mapToEntity(book: BookDocument): BookEntity =
        BookEntity(
            bookId = book.id,
            title = book.title,
            description = book.description,
            publishingYear = book.publishingYear,
            pageCount = book.pageCount,
            thumbnailUrl = book.thumbnailUrl,
            smallThumbnailUrl = book.smallThumbnailUrl,
            authors = book.authors?.takeIf { it.isNotEmpty() }?.map {
                mapAuthorToEntity(it)
            }?.toMutableSet() ?: mutableSetOf(),
            genres = book.genres?.takeIf { it.isNotEmpty() }?.map {
                mapGenreToEntity(it)
            }?.toMutableSet() ?: mutableSetOf()
        )

    fun BookDocument.mapToEntity(authors: Set<AuthorEntity>, genres: Set<GenreEntity>): BookEntity =
        BookEntity(
            bookId = id,
            title = title,
            description = description,
            publishingYear = publishingYear,
            pageCount = pageCount,
            thumbnailUrl = thumbnailUrl,
            smallThumbnailUrl = smallThumbnailUrl,
            authors = authors.toMutableSet(),
            genres = genres.toMutableSet(),
        )

    fun RatingEmbedded.mapToEntity(book: BookEntity, sourceEntity: RatingSourceEntity): RatingEntity =
        RatingEntity(
            book = book,
            score = score,
            count = count,
            source = sourceEntity
        )

    fun RatingSourceEmbedded.mapToEntity(): RatingSourceEntity =
        RatingSourceEntity(
            name = name,
            url = url
        )

    fun mapAuthorToEntity(author: AuthorEmbedded) =
        AuthorEntity(
            publicId = AuthorDocument.generatePublicId(author.key, author.name),
            name = author.name,
            otherNames = author.otherNames,
        )

    fun mapGenreToEntity(genre: GenreEmbedded) =
        GenreEntity(
            name = genre.name,
        )

    fun map(bookEntity: BookEntity, sourceEntity: RatingSourceEntity, rating: RatingDocument): RatingEntity =
        RatingEntity(
            id = null,
            score = rating.score,
            count = rating.count,
            book = bookEntity,
            source = sourceEntity
        )

    fun map(bookEntity: BookEntity, sourceEntity: RatingSourceEntity, rating: RatingEmbedded): RatingEntity =
        RatingEntity(
            id = null,
            score = rating.score,
            count = rating.count,
            book = bookEntity,
            source = sourceEntity
        )

    fun map(ratingSource: RatingSourceEmbedded) =
        RatingSourceEntity(
            id = null,
            name = ratingSource.name,
            url = ratingSource.url
        )

    fun RatingUpdate.toRatingEmbedded() = RatingEmbedded(
        id = this.id,
        count = this.count,
        score = this.score,
        source = this.source.toRatingSourceEmbedded()
    )

    fun RatingSourceUpdate.toRatingSourceEmbedded() = RatingSourceEmbedded(
        name = this.name,
        url = this.url
    )

    @OptIn(ExperimentalUuidApi::class)
    fun mapToEntity(book: Book): BookEntity {
        return BookEntity(
            bookId = book.id,
            title = book.title,
            description = book.description,
            publishingYear = book.publishingYear.value,
            pageCount = book.pageCount,
            thumbnailUrl = book.thumbnailUrl,
            smallThumbnailUrl = book.smallThumbnailUrl,
            authors = book.authors.takeIf { it.isNotEmpty() }?.map {
                AuthorEntity(
                    publicId = it.publicId,
                    name = it.name,
                    otherNames = it.otherNames
                )
            }?.toMutableSet() ?: mutableSetOf(),
            genres = book.genres.takeIf { it.isNotEmpty() }?.map {
                GenreEntity(
                    id = null,
                    name = it.name
                )
            }?.toMutableSet() ?: mutableSetOf()
        )
    }

    fun map(bookEntity: BookEntity, sourceEntity: RatingSourceEntity, rating: Rating): RatingEntity =
        RatingEntity(
            id = null,
            score = rating.score,
            count = rating.count,
            book = bookEntity,
            source = sourceEntity
        )

    fun map(ratingSource: RatingSource) =
        RatingSourceEntity(
            id = null,
            name = ratingSource.name,
            url = ratingSource.url
        )


    fun map(book: Book): BookDocument =
        BookDocument(
            id = book.id,
            title = book.title,
            description = book.description,
            publishingYear = book.publishingYear.value,
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
                    name = it.name,
                    key = it.key
                )
            },
            genres = book.genres.map {
                GenreEmbedded(
                    name = it.name
                )
            },
            createdOn = LocalDateTime.now(),
            ratings = book.ratings.map {
                RatingEmbedded(
                    score = it.score,
                    count = it.count,
                    source = RatingSourceEmbedded(
                        name = it.source.name,
                        url = it.source.url
                    )
                )
            }.toSet()
        )

    fun map(book: BookEntity): BookSearchResponse = BookSearchResponse(
        id = book.bookId!!,
        title = book.title,
        smallThumbnailUrl = book.smallThumbnailUrl
    )

    fun BookEntity.toDetailsDto() = BookDetailsDto(
        bookId = bookId,
        title = title,
        publishedYear = publishingYear,
        description = description,
        publishingYear = publishingYear,
        pageCount = pageCount,
        thumbnailUrl = thumbnailUrl,
        smallThumbnailUrl = smallThumbnailUrl
    )

    fun BookEntity.toDto() = BookDto(
        id = bookId,
        title = title,
        smallThumbnailUrl = smallThumbnailUrl,
        publishedYear = publishingYear
    )

    fun map(book: BookDocument) = BookSearchResponse(
        id = book.id,
        title = book.title,
        smallThumbnailUrl = book.smallThumbnailUrl
    )

    fun map(details: OpenLibraryBookDetails): BookUpdate = BookUpdate(
        id = details.key,
        description = details.description?.value,
        genres = details.subjects?.map { subject ->
            GenreUpdate(
                id = null,
                name = subject
            )
        }
    )

    fun map(
        openDetails: OpenLibraryBookDetails?, openDoc: OpenLibraryDoc?, archiveDetails: ArchiveSearchDoc?,
        basicInfo: BookBasicInfoDocument
    ) = Book(
        id = basicInfo.bookPublicId,
        title = basicInfo.title,
        description = openDetails?.description?.value ?: archiveDetails?.description?.firstOrNull(),
        publishingYear = (openDoc?.firstPublishYear?.let { Year.of(it) }
            ?: archiveDetails?.year?.let { Year.of(it) })!!,
        pageCount = openDetails?.numberOfPages,
        edition = BookEdition(basicInfo.editionTitle, basicInfo.lang),
        //TODO fix for publicID together witth key
        authors = openDoc?.authorName?.map { name -> Author(name = name, key = "", publicId = "", otherNames = null) }
            ?.takeIf { it.isNotEmpty() }
            ?: archiveDetails?.creator?.map { name -> Author(name = name, key = "", publicId = "", otherNames = null) }
            ?: emptyList(),
        genres = openDetails?.subjects?.map { Genre(name = it) } ?: emptyList(),
        ratings = emptyList()
    )

    fun map(item: BookItem, basicInfo: BookBasicInfoDocument): Book = Book(
        id = basicInfo.bookPublicId,
        title = item.volumeInfo.title,
        description = item.volumeInfo.description ?: "",
        publishingYear = item.volumeInfo.publishedDate.take(4).toInt().let { Year.of(it) },
        pageCount = item.volumeInfo.pageCount ?: 0,
        edition = BookEdition(basicInfo.editionTitle, basicInfo.lang),
        thumbnailUrl = item.volumeInfo.imageLinks?.thumbnail ?: "",
        smallThumbnailUrl = item.volumeInfo.imageLinks?.smallThumbnail ?: "",
        authors = item.volumeInfo.authors?.map { name ->
            Author(
                name = name,
                //TODO fix for publicID together witth key
                publicId = "",
                key = ""
            )
        } ?: emptyList(),
        genres = item.volumeInfo.categories?.map { name -> Genre(name = name) }
            ?: emptyList(),
        ratings = emptyList()
    )

    fun map(bookBasicInfo: BookBasicInfoDocument) =
        BookBasicInfoDto(
            id = bookBasicInfo.id,
            title = bookBasicInfo.title,
            bookId = bookBasicInfo.bookPublicId,
            firstPublishDate = bookBasicInfo.firstPublishDate
        )

    fun mapDetails(bookBasicInfo: BookBasicInfoDocument) =
        BookBasicInfoDetailsDto(
            id = bookBasicInfo.id,
            bookId = bookBasicInfo.bookPublicId,
            title = bookBasicInfo.title,
            openLibraryKey = bookBasicInfo.openLibraryKey,
            openLibraryEditionKey = bookBasicInfo.openLibraryEditionKey,
            editionTitle = bookBasicInfo.editionTitle,
            firstPublishYear = bookBasicInfo.firstPublishYear,
            firstPublishDate = bookBasicInfo.firstPublishDate,
            authors = bookBasicInfo.authors,
            authorsKeys = bookBasicInfo.authorsKeys,
            lang = bookBasicInfo.lang,
            subjects = bookBasicInfo.subjects,
            description = bookBasicInfo.description
        )


    fun map(authorEntity: AuthorEntity): AuthorDto =
        AuthorDto(
            id = authorEntity.id,
            publicId = authorEntity.publicId,
            name = authorEntity.name
        )

    fun mapDetails(authorEntity: AuthorEntity): AuthorDetailsDto =
        AuthorDetailsDto(
            id = authorEntity.id,
            publicId = authorEntity.publicId,
            name = authorEntity.name
        )
}
