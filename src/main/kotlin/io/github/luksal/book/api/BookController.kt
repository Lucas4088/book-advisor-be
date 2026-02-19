package io.github.luksal.book.api

import io.github.luksal.book.api.dto.BookSearchCriteriaRequest
import io.github.luksal.book.api.dto.BookSearchResponse
import io.github.luksal.book.service.BookDataPopulationService
import io.github.luksal.book.service.BookService
import io.github.luksal.book.service.SyncBookDataService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
    private val bookDataPopulationService: BookDataPopulationService,
    private val syncBookDataService: SyncBookDataService,
    private val customInitializerDispatcher: CoroutineDispatcher,
) {

    @GetMapping(params = ["title", "genres", "publishedYearRange"])
    fun search(request: BookSearchCriteriaRequest, page: Pageable): Page<BookSearchResponse> {
        return bookService.searchBookDocuments(criteria = request.toServiceDto(), pageable = page)
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
        CoroutineScope(customInitializerDispatcher).launch {
            bookDataPopulationService.populateBasicBookInfoCollection()
        }
    }

    @PostMapping(path = ["/populate"])
    fun populateBooksCollection() {
        CoroutineScope(customInitializerDispatcher).launch {
            bookDataPopulationService.populateBooksCollection()
        }
    }


    //TODO add also job
    @PostMapping(path = ["/sync"])
    fun syncBooks() {
        return syncBookDataService.syncBookData()
    }
}