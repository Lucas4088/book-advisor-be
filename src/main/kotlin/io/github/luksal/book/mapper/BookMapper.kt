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

object BookMapper {
    fun BookDocument.mapToEntity(): BookEntity =
        BookEntity(
            bookId = id,
            title = title,
            description = description,
            language = language,
            publishingYear = publishingYear,
            pageCount = pageCount,
            thumbnailUrl = thumbnailUrl,
            smallThumbnailUrl = smallThumbnailUrl,
            authors = authors?.takeIf { it.isNotEmpty() }?.map {
                mapAuthorToEntity(it)
            }?.toMutableSet() ?: mutableSetOf(),
            genres = genres?.takeIf { it.isNotEmpty() }?.map {
                mapGenreToEntity(it)
            }?.toMutableSet() ?: mutableSetOf()
        )

    fun BookDocument.mapToEntity(authors: Set<AuthorEntity>, genres: Set<GenreEntity>): BookEntity =
        BookEntity(
            bookId = id,
            title = title,
            description = description,
            language = language,
            publishingYear = publishingYear,
            pageCount = pageCount,
            thumbnailUrl = thumbnailUrl,
            smallThumbnailUrl = smallThumbnailUrl,
            authors = authors.toMutableSet(),
            genres = genres.toMutableSet(),
        )

    fun BookDocument.mapToEditionEntity(bookEntity: BookEntity): BookEditionEntity =
        BookEditionEntity(
            title = title,
            language = language!!,
            book = bookEntity,
        )

    fun RatingEmbedded.mapToEntity(book: BookEntity, sourceEntity: RatingSourceEntity): RatingEntity =
        RatingEntity(
            book = book,
            score = score,
            count = count,
            source = sourceEntity,
            titleConfidenceIndicator = titleConfidenceIndicator,
            authorsConfidenceIndicator = authorsConfidenceIndicator,
        )

    fun RatingSourceEmbedded.mapToEntity(): RatingSourceEntity =
        RatingSourceEntity(
            name = name,
            url = url
        )

    fun RatingDocument.mapToEntity(book: BookEntity, source: RatingSourceEntity): RatingEntity =
        RatingEntity(
            book = book,
            score = score,
            count = count,
            source = source,
            titleConfidenceIndicator = titleConfidenceIndicator,
            authorsConfidenceIndicator = authorsConfidenceIndicator,
        )

    fun RatingUpdate.toDocument(bookId: String) =
        RatingDocument(
            bookId = bookId,
            score = score,
            count = count,
            source = RatingSourceEmbedded(
                name = source.name,
                url = source.url
            ),
            titleConfidenceIndicator = titleConfidenceIndicator,
            authorsConfidenceIndicator = authorsConfidenceIndicator,
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

    fun RatingUpdate.toRatingEmbedded() = RatingEmbedded(
        id = id,
        count = count,
        score = score,
        source = source.toRatingSourceEmbedded(),
        titleConfidenceIndicator = titleConfidenceIndicator,
        authorsConfidenceIndicator = authorsConfidenceIndicator,
    )

    fun RatingSourceUpdate.toRatingSourceEmbedded() = RatingSourceEmbedded(
        name = this.name,
        url = this.url
    )


    fun Book.toDocument(): BookDocument =
        BookDocument(
            id = id,
            title = title,
            language = lang,
            description =  description,
            publishingYear = publishingYear.value,
            pageCount = pageCount,
            openLibraryKey = key,
            isEdition = isEdition,
            edition = edition?.let {
                EditionEmbedded(
                    title = it.title,
                    lang = it.lang
                )
            },
            thumbnailUrl = thumbnailUrl,
            smallThumbnailUrl = smallThumbnailUrl,
            authors = authors.map {
                AuthorEmbedded(
                    name = it.name,
                    key = it.key
                )
            },
            genres = genres.map {
                GenreEmbedded(
                    name = it.name
                )
            },
            createdOn = LocalDateTime.now(),
            ratings = ratings.map {
                RatingEmbedded(
                    score = it.score,
                    count = it.count,
                    source = RatingSourceEmbedded(
                        name = it.source.name,
                        url = it.source.url
                    ),
                    titleConfidenceIndicator = it.titleConfidenceIndicator,
                    authorsConfidenceIndicator = it.authorsConfidenceIndicator,
                )
            }.toSet()
        )

    fun BookEntity.toDetailsDto(rating: RatingResult? = null) = BookDetailsDto(
        bookId = bookId,
        title = title,
        publishedYear = publishingYear,
        description = description,
        publishingYear = publishingYear,
        authors = authors.map { it.toDto() },
        pageCount = pageCount,
        thumbnailUrl = thumbnailUrl,
        smallThumbnailUrl = smallThumbnailUrl,
        rating =  rating
    )

    fun BookEntity.toDto() = BookDto(
        id = bookId,
        title = title,
        smallThumbnailUrl = smallThumbnailUrl,
        publishedYear = publishingYear,
    )

    fun BookDocument.mapToSearchResponse() = BookSearchResponse(
        id = id,
        title = title,
        authors = authors?.map { it.name }.orEmpty(),
        language = language,
        smallThumbnailUrl = smallThumbnailUrl,
    )

    fun BookBasicInfoDocument.toModel(
        openDetails: OpenLibraryBookDetails?, openDoc: OpenLibraryDoc?, archiveDetails: ArchiveSearchDoc?,
        lang: String?
    ) = Book(
        id = bookPublicId,
        title = title,
        description = openDetails?.description?.value ?: archiveDetails?.description?.firstOrNull(),
        publishingYear = (openDoc?.firstPublishYear?.let { Year.of(it) }
            ?: archiveDetails?.year?.let { Year.of(it) })!!,
        pageCount = openDetails?.numberOfPages,
        key = openLibraryKey,
        lang = lang,
        isEdition = isEdition,
        edition = BookEdition(editionTitle, lang ?: ""),
        //TODO fix for publicID together witth key
        authors = openDoc?.authorName?.map { name -> Author(name = name, key = "", publicId = "", otherNames = null) }
            ?.takeIf { it.isNotEmpty() }
            ?: archiveDetails?.creator?.map { name -> Author(name = name, key = "", publicId = "", otherNames = null) }
            ?: emptyList(),
        genres = openDetails?.subjects?.map { Genre(name = it) } ?: emptyList(),
        ratings = emptyList()
    )

    fun BookItem.toModel(basicInfo: BookBasicInfoDocument, lang: String?) = Book(
        id = basicInfo.bookPublicId,
        title = volumeInfo.title,
        description = volumeInfo.description ?: "",
        publishingYear = volumeInfo.publishedDate.take(4).toInt().let { Year.of(it) },
        pageCount = volumeInfo.pageCount ?: 0,
        key = basicInfo.openLibraryKey,
        isEdition = basicInfo.isEdition,
        edition = BookEdition(basicInfo.editionTitle, ""),
        thumbnailUrl = volumeInfo.imageLinks?.thumbnail ?: "",
        smallThumbnailUrl = volumeInfo.imageLinks?.smallThumbnail ?: "",
        lang = lang,
        authors = volumeInfo.authors?.map { name ->
            Author(
                name = name,
                //TODO fix for publicID together witth key
                publicId = "",
                key = ""
            )
        } ?: emptyList(),
        genres = volumeInfo.categories?.map { name -> Genre(name = name) }
            ?: emptyList(),
        ratings = emptyList()
    )

    fun BookBasicInfoDocument.toDto() =
        BookBasicInfoDto(
            id = id,
            title = title,
            bookId = bookPublicId,
            firstPublishDate = firstPublishDate
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


    fun AuthorEntity.toDto(): AuthorDto =
        AuthorDto(
            id = id,
            publicId = publicId,
            name = name
        )

    fun AuthorEntity.mapDetails(): AuthorDetailsDto =
        AuthorDetailsDto(
            id = id,
            publicId = publicId,
            name = name
        )
}
