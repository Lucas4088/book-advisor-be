package io.github.luksal.ingestion.file.api

import io.github.luksal.ingestion.file.service.FileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/file-import/open-library")
class FileImportController(
    private val fileService: FileService
) {

    @GetMapping("/{eventName}")
    fun getFileImportInitState(@PathVariable("eventName") eventName: String): String? =
        fileService.loadInitFileImportState(eventName)

    @PostMapping("/author")
    fun importAuthors(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            fileService.importAuthors()
        }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/book-basic-info", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun importBookBasicInfo(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            fileService.importBookBasicInfo()
        }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/book-basic-info-edition", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun importBookBasicInfoEditions(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            fileService.importBookBasicInfoEditions()
        }
        return ResponseEntity.ok().build()
    }
}