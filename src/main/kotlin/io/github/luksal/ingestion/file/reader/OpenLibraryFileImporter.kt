package io.github.luksal.ingestion.file.reader

import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.book.service.AuthorService
import io.github.luksal.book.service.BookService
import io.github.luksal.event.service.EventService
import io.github.luksal.util.ext.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Component
class OpenLibraryFileImporter(
    @Value($$"${app.file-read.path.authors}") private val authorsFilePath: String,
    @Value($$"${app.file-read.path.works}") private val worksFilePath: String,
    private val authorService: AuthorService,
    private val bookService: BookService,
    private val eventService: EventService
) {

    companion object {
        val log = logger()
    }

    fun readAndSaveAuthors() {
        val authors = mutableListOf<AuthorDocument>()
        readFileWithProgress(authorsFilePath, "authors-import") { line: String ->
            val parts = line.split('\t', limit = 5)
            JsonMapper().readTree(parts[4]).let { jsonNode ->
                val name = jsonNode.get("name")?.asString()
                val id = jsonNode.get("key").asString()

                if (name != null) authors.add(AuthorDocument(id, name))

                if (authors.size >= 5000) {
                    authorService.saveAuthorDocuments(authors)
                    authors.clear()
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun readAndSaveBookBasicInfo() {
        val bookBasicInfo = mutableListOf<BookBasicInfoDocument>()
        readFileWithProgress(worksFilePath, "book-basic-info-import") { line: String ->
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

    private fun readFileWithProgress(
        filePath: String,
        eventName: String,
        processLine: (String) -> Unit
    ) {
        val file = File(filePath)
        val totalSizeBytes = file.length().toDouble()

        var readBytes = 0L
        val logStepBytes = 10L * 1024 * 1024 // 10 MB
        var nextLogThreshold = logStepBytes

        file.useLines { lines ->
            lines.forEach { line ->
                readBytes += line.toByteArray().size + 1
                processLine(line)
                if (readBytes >= nextLogThreshold) {
                    val progress = readBytes.toDouble() / totalSizeBytes * 100
                    val formattedProgress = "%.2f".format(progress)
                    eventService.sendEvent(eventName, formattedProgress)
                    log.info("File $filePath progress: $formattedProgress%")
                    nextLogThreshold += logStepBytes
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
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
        }?.map { it.name }  ?: emptyList()
        return title?.let {
            BookBasicInfoDocument(
                openLibraryKey = key ?: "",
                publicId = Uuid.generateV7().toString(),
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

