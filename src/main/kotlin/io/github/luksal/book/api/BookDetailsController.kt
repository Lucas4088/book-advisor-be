package io.github.luksal.book.api

import io.github.luksal.book.api.dto.BookDetailsDto
import io.github.luksal.book.api.dto.BookDto
import io.github.luksal.book.api.dto.BookSearchCriteria
import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.service.BookDataPurgeService
import io.github.luksal.book.service.BookService
import io.github.luksal.book.service.SyncBookDataService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/book")
class BookDetailsController(
    private val bookService: BookService,
    private val syncBookDataService: SyncBookDataService,
    private val bookDataPurgeService: BookDataPurgeService
) {

    @PostMapping
    fun search(@RequestBody request: BookSearchCriteria, page: Pageable): Page<BookDto> =
        bookService.searchBooks(request.toServiceDto(), page)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): BookDetailsDto =
        bookService.getBookById(id)

    @GetMapping("/{id}/crawl")
    fun getByIdForCrawling(@PathVariable id: String): BookSearchResponse =
        bookService.getBookByIdForCrawling(id = id)

    @PostMapping(path = ["/sync"])
    fun syncBooks() {
        return syncBookDataService.syncBookData()
    }

    @DeleteMapping("/purge")
    fun purgeBooks(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            bookDataPurgeService.purgeBooks()
        }
        return ResponseEntity.ok().build()
    }
}