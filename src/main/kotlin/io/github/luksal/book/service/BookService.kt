package io.github.luksal.book.service

import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.db.document.BookBasicInfoRepository
import io.github.luksal.book.db.document.BookDocumentRepository
import io.github.luksal.book.db.document.model.BookBasicInfoDocument
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.openlibrary.api.OpenLibraryService
import io.github.luksal.book.openlibrary.api.dto.OpenLibraryDoc
import io.github.luksal.book.service.dto.BookSearchCriteriaDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookJpaRepository: BookJpaRepository,
    private val bookBasicInfoRepository: BookBasicInfoRepository,
    private val bookDocumentRepository: BookDocumentRepository,
    private val openLibraryService: OpenLibraryService,
    private val ioInitializerDispatcher: kotlinx.coroutines.CoroutineDispatcher
) {

    fun searchBooks(criteria: BookSearchCriteriaDto, pageable: Pageable): Page<BookSearchResponse> {
        return bookJpaRepository.searchAll(
            title = criteria.title,
            startYear = criteria.publishedYearRange.first,
            endYear = criteria.publishedYearRange.last,
            genres = criteria.genres.map { it.name }.toList(),
            pageable = pageable
        ).map { BookSearchResponse.fromEntity(book = it) }
    }

    fun getBookById(id: Long): BookSearchResponse {
        return bookJpaRepository.findById(id).map { BookSearchResponse.fromEntity(book = it) }
            .orElseThrow()
    }

    fun initBasicBookInfoCollection(fromYear: Int, toYear: Int, lang: String) {
        //TODO("not async fix")
        CoroutineScope(ioInitializerDispatcher).launch {
            var page = 0
            val limit = 100
            do {
                page++
                val response = openLibraryService.searchBooks(fromYear, toYear, lang, page, limit)
                saveBookBasicInfo(response.docs, lang)
            } while (response.docs.isNotEmpty())
        }
        //TODO("Send email about results of the operation")
    }

    private fun saveBookBasicInfo(bookBasicInfo: List<OpenLibraryDoc> = emptyList(), lang: String) {
        val documents = bookBasicInfo.map { BookBasicInfoDocument(
            id = it.key ?: "",
            title = it.title,
            key = it.key ?: "",
            editionTitle = it.editions?.docs?.first()?.title,
            editionKey = it.editions?.docs?.first()?.key,
            lang = lang
        ) }
        bookBasicInfoRepository.saveAll(documents)
    }
}