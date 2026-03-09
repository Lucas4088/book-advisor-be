package io.github.luksal.ingestion.file.api

import io.github.luksal.ingestion.file.reader.OpenLibraryFileImporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/file-import/open-library")
class FileImportController(
    private val openLibraryFileImporter: OpenLibraryFileImporter
) {

    @PostMapping("/author")
    fun importAuthors(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            openLibraryFileImporter.readAndSaveAuthors()
        }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/book-basic-info", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun importBookBasicInfo(): ResponseEntity<Unit> {
        CoroutineScope(Dispatchers.IO).launch {
            openLibraryFileImporter.readAndSaveBookBasicInfo()
        }
        return ResponseEntity.ok().build()
    }
}