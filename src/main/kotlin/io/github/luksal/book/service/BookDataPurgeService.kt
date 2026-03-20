package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.event.PopulateBookBasicDataJpaRepository
import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.book.db.jpa.event.SyncBookEventJpaRepository
import io.github.luksal.ingestion.crawler.jpa.ScheduledBookCrawlerEventRepository
import io.github.luksal.ingestion.file.service.FileService
import io.github.luksal.integration.db.BookDetailsFetchedEventRepository
import io.github.luksal.util.ext.logger
import jakarta.transaction.Transactional
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class BookDataPurgeService(
    private val bookService: BookService,
    private val bookBasicDataPopulationJpaRepository: PopulateBookBasicDataJpaRepository,
    private val bookDetailsEventJpaRepository: PopulateBookDetailsEventJpaRepository,
    private val scheduledBookCrawlerEventRepository: ScheduledBookCrawlerEventRepository,
    private val bookDetailsFetchedEventRepository: BookDetailsFetchedEventRepository,
    private val syncBookEventJpaRepository: SyncBookEventJpaRepository,
    @Value($$"${app.kafka.connect.books-source-topic}")
    private val sourceTopic: String? = null,
    private val adminClient: AdminClient,
    private val fileService: FileService
) {

    companion object {
        private val log = logger()
    }

    @Transactional
    fun purgeBooks() {
        log.info("Deleting all Book details")
        bookService.deleteAllBookDetails()
        log.info("Deleting all book crawler events")
        scheduledBookCrawlerEventRepository.deleteAllInBatch()
        log.info("Deleting all book sync events")
        syncBookEventJpaRepository.deleteAllInBatch()
        log.info("Resetting book stream kafka offset")
        resetBookSyncKafkaOffset()
    }

    @Transactional
    fun purgeBookBasicInfo() =
        try {
            log.info("Deleting all book basic info documents")
            bookService.deleteAllBookBasicInfo()
            log.info("Deleting all book details populate events")
            bookDetailsEventJpaRepository.deleteAllInBatch()
            log.info("Resetting file import starting pointer")
            fileService.resetInitBookBasicFileImportState()
            log.info("Deleting all book details Fetched events")
            bookDetailsFetchedEventRepository.deleteAllInBatch()
        } catch (e: Exception) {
            log.error("Error while purging book basic info", e)
            throw e
        }

    private fun resetBookSyncKafkaOffset() {
        val partitions = adminClient
            .describeTopics(listOf(sourceTopic))
            .allTopicNames()
            .get()[sourceTopic]!!
            .partitions()
            .map { TopicPartition(sourceTopic, it.partition()) }
        val offsets = partitions.associateWith {
            OffsetAndMetadata(0) // 0 = earliest
        }
        adminClient.alterConsumerGroupOffsets("book-advisor-consumer", offsets)
    }
}