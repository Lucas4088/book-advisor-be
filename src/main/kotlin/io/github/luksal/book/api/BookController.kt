package io.github.luksal.book.api

import io.github.luksal.book.api.dto.BookSearchCriteriaRequest
import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.service.BookService
import io.github.luksal.book.service.SyncBookDataService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
    private val syncBookDataService: SyncBookDataService,
) {

    @GetMapping(params = ["title", "genres", "publishedYearRange"])
    fun search(request: BookSearchCriteriaRequest, page: Pageable): Page<BookSearchResponse> {
        return bookService.searchBookDocuments(criteria = request.toServiceDto(), pageable = page)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): BookSearchResponse {
        return bookService.getBookById(id = id)
    }

    //TODO add kafka stream processing
    @PostMapping(path = ["/sync"])
    fun syncBooks() {
        return syncBookDataService.syncBookData()
    }
}