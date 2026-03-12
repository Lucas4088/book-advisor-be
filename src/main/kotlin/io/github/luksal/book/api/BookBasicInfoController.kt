package io.github.luksal.book.api

import io.github.luksal.book.service.BookDataPurgeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/book-basic-info")
class BookBasicInfoController(private val bookDataPurgeService: BookDataPurgeService) {

    @DeleteMapping("/purge")
    fun purgeBookBasicInfo(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            bookDataPurgeService.purgeBookBasicInfo()
        }
        return ResponseEntity.ok().build()
    }

}