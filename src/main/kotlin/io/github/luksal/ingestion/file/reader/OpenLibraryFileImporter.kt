package io.github.luksal.ingestion.file.reader

import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument.Companion.generatePublicId
import io.github.luksal.book.service.AuthorService
import io.github.luksal.book.service.BookService
import io.github.luksal.event.service.EventService
import io.github.luksal.ingestion.file.dto.FileImportState
import io.github.luksal.util.ext.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.io.File
import java.io.RandomAccessFile

@Component
class OpenLibraryFileImporter(
    @Value($$"${app.file-read.path.authors}") private val authorsFilePath: String,
    @Value($$"${app.file-read.path.works}") private val worksFilePath: String,
    private val authorService: AuthorService,
    private val bookService: BookService,
    private val eventService: EventService,
    private val redisTemplate: RedisTemplate<Any, Any>
) {

    companion object {
        const val AUTHORS_IMPORT_EVENT_NAME = "authors-import"
        const val BOOK_BASIC_INFO_EVENT_IMPORT_NAME = "book-basic-info-import"
        val log = logger()
    }

    fun readAndSaveAuthors() {
        val authors = mutableListOf<AuthorDocument>()
        readFileWithOffsetAndProgress(authorsFilePath, AUTHORS_IMPORT_EVENT_NAME, loadProgressState(AUTHORS_IMPORT_EVENT_NAME).filePointer) { line: String ->
            val parts = line.split('\t', limit = 5)
            JsonMapper().readTree(parts[4]).let { jsonNode ->
                val name = jsonNode.get("name")?.asString()
                val key = jsonNode.get("key").asString()

                if (name != null) authors.add(
                    AuthorDocument(
                        publicId = AuthorDocument.generatePublicId(key, name),
                        key = key,
                        name = name
                    )
                )

                if (authors.size >= 5000) {
                    authorService.saveAuthorDocuments(authors)
                    authors.clear()
                }
            }
        }
    }

    fun readAndSaveBookBasicInfo() {
        val bookBasicInfo = mutableListOf<BookBasicInfoDocument>()
        readFileWithOffsetAndProgress(worksFilePath, BOOK_BASIC_INFO_EVENT_IMPORT_NAME, loadProgressState(BOOK_BASIC_INFO_EVENT_IMPORT_NAME).filePointer) { line: String ->
            val parts = line.split('\t', limit = 5)
            JsonMapper().readTree(parts[4]).let { jsonNode ->
                jsonNode?.let {
                    createBookBasicInfoDocument(jsonNode)?.let {
                        bookBasicInfo.add(it)
                    }
                    if (bookBasicInfo.size >= 5000) {
                        bookService.saveBookBasicInfo(bookBasicInfo)
                        bookBasicInfo.clear()
                    }
                }
            }
        }
    }

    fun loadProgressState(eventName: String) : FileImportState {
        val value = redisTemplate.opsForValue()[eventName]
        return ObjectMapper().convertValue(value, FileImportState::class.java) ?: FileImportState()
    }

    fun saveProgressState(eventName: String, offset: Long, formattedProgress: String) {
        redisTemplate.opsForValue()[eventName] = FileImportState(offset, formattedProgress)
    }

    private fun readFileWithOffsetAndProgress(
        filePath: String,
        eventName: String,
        startOffset: Long = 0,
        processLine: (String) -> Unit
    ) {
        val file = File(filePath)
        val totalSizeBytes = file.length().toDouble()

        var readBytes = startOffset
        val logStepBytes = 10L * 1024 * 1024 // 10 MB
        var nextLogThreshold = ((startOffset / logStepBytes) + 1) * logStepBytes

        var formattedProgress = ""
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(startOffset)

            var line: String?

            while (raf.readLine().also { line = it } != null) {
                processLine(line!!)

                readBytes = raf.filePointer

                val progress = readBytes.toDouble() / totalSizeBytes * 100
                formattedProgress = "%.2f".format(progress)
                if (readBytes >= nextLogThreshold) {
                    eventService.emit(eventName, formattedProgress)
                    log.info("File $filePath progress: $formattedProgress%")

                    nextLogThreshold += logStepBytes
                }
                saveProgressState(eventName, raf.filePointer, formattedProgress)
            }

        }
    }

    private fun createBookBasicInfoDocument(
        jsonNode: JsonNode,
    ): BookBasicInfoDocument? {
        val title = jsonNode.get("title")?.asString()
        val subjects = jsonNode.get("subjects")?.mapNotNull { it.asString() }
        val authorsKeys = jsonNode.get("authors")?.mapNotNull { it.get("author")?.get("key")?.asString() }
        val key = jsonNode.get("key")?.asString()
        val desc = jsonNode.get("description")?.get("value")?.asString()
        val firstPublishDate = jsonNode.get("first_publish_date")?.asString()

        val authors = authorsKeys?.let {
            authorService.getAuthors(it)
        }?.map { it.name } ?: emptyList()
        return title?.let {
            BookBasicInfoDocument(
                openLibraryKey = key ?: "",
                bookPublicId = generatePublicId(title, authors),
                title = title,
                openLibraryEditionKey = key,
                subjects = subjects ?: emptyList(),
                authors = authors,
                authorsKeys = authorsKeys ?: emptyList(),
                firstPublishDate = firstPublishDate,
                description = desc,
                lang = "eng"
            )
        }
    }
}

