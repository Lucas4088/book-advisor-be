package io.github.luksal.book.api

import io.github.luksal.book.api.dto.BookSearchCriteriaRequest
import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.service.BookDataPopulationService
import io.github.luksal.book.service.BookService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RestController
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
    private val bookDataPopulationService: BookDataPopulationService
) {

    @GetMapping(params = ["title", "genres", "publishedYearRange"])
    fun search(request: BookSearchCriteriaRequest, page: Pageable): Page<BookSearchResponse> {
        return bookService.searchBooks(criteria = request.toServiceDto(), pageable = page)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): BookSearchResponse {
        return bookService.getBookById(id = id)
    }

    @PostMapping(path = ["/basic-info/schedule-population"], params = ["fromYear", "toYear", "lang"])
    fun scheduleBookBasicDataPopulation(fromYear: Int, toYear: Int, lang: String) {
        return bookDataPopulationService.scheduleBasicBookInfoCollection(
            fromYear = fromYear,
            toYear = toYear,
            lang = lang
        )
    }

    @PostMapping(path = ["/basic-info/populate"])
    fun populateBasicBookInfoCollection() {
        return bookDataPopulationService.populateBasicBookInfoCollection()
    }

    @PostMapping(path = ["/populate"])
    fun populateBooksCollection() {
        return bookDataPopulationService.populateBooksCollection()
    }
}