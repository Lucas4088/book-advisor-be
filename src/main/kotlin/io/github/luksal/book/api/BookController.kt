package io.github.luksal.book.api

import io.github.luksal.book.api.dto.BookSearchCriteriaRequest
import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.service.BookService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Controller
@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    @GetMapping(params = ["title", "genres", "publishedYearRange"])
    fun search(request: BookSearchCriteriaRequest, page: Pageable): Page<BookSearchResponse> {
        return bookService.searchBooks(criteria = request.toServiceDto(), pageable = page)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): BookSearchResponse {
        return bookService.getBookById(id = id)
    }

    @PostMapping(path = ["/init-basic-info"], params = ["fromYear", "toYear", "lang"])
    fun initBasicBookInfoCollection(fromYear: Int, toYear: Int, lang: String) {
        return bookService.initBasicBookInfoCollection(fromYear = fromYear, toYear = toYear, lang = lang)
    }
}