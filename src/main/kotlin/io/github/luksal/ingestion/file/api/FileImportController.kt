package io.github.luksal.ingestion.file.api

import io.github.luksal.event.service.EventService
import io.github.luksal.ingestion.file.reader.OpenLibraryFileImporter
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/file-import/open-library")
class FileImportController(
    private val openLibraryFileImporter: OpenLibraryFileImporter
) {

    @PostMapping("/author")
    suspend fun importAuthors() =
        openLibraryFileImporter.readAndSaveAuthors()

    @PostMapping("/book-basic-info", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun importBookBasicInfo() =
        openLibraryFileImporter.readAndSaveBookBasicInfo()
}