package io.github.luksal.book.api

import io.github.luksal.book.api.dto.BookBasicInfoDetailsDto
import io.github.luksal.book.api.dto.BookBasicInfoDto
import io.github.luksal.book.api.dto.BookBasicInfoSearchCriteria
import io.github.luksal.book.service.BookDataPurgeService
import io.github.luksal.book.service.BookService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/book-basic-info")
class BookBasicInfoController(
    private val bookService: BookService,
    private val bookDataPurgeService: BookDataPurgeService
) {

    @PostMapping
    fun search(@RequestBody criteria: BookBasicInfoSearchCriteria, page: Pageable): Page<BookBasicInfoDto> =
        bookService.searchBookBasicInfo(criteria, page)


    @GetMapping("/{bookId}")
    fun getById(@PathVariable bookId: String): BookBasicInfoDetailsDto? =
        bookService.getBookBasicInfoById(bookId)

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String) = {}


    @DeleteMapping("/purge")
    fun purgeBookBasicInfo(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            bookDataPurgeService.purgeBookBasicInfo()
        }
        return ResponseEntity.ok().build()
    }

}