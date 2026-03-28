package io.github.luksal.ingestion.file.service

import io.github.luksal.ingestion.file.reader.OpenLibraryFileImporter
import org.springframework.stereotype.Component

@Component
class FileService(
    private val openLibraryFileImporter: OpenLibraryFileImporter
) {

    fun importAuthors() =
        openLibraryFileImporter.readAndSaveAuthors()

    fun importBookBasicInfo() =
        openLibraryFileImporter.readAndSaveBookBasicInfo()

    fun importBookBasicInfoEditions() =
        openLibraryFileImporter.readAndSaveBookBasicInfoForEdition()

    fun loadInitFileImportState(eventName: String): String? =
        openLibraryFileImporter.loadProgressState(eventName).formattedProgress

    fun resetInitBookBasicFileImportState() =
        openLibraryFileImporter.saveProgressState(OpenLibraryFileImporter.BOOK_BASIC_INFO_EVENT_IMPORT_NAME, 0, "0")
}