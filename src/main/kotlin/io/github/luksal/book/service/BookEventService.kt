package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.event.PopulateBookBasicDataJpaRepository
import io.github.luksal.book.db.jpa.event.PopulateBookDetailsEventJpaRepository
import io.github.luksal.book.db.jpa.event.SyncBookEventJpaRepository
import org.springframework.stereotype.Service

@Service
class BookEventService(
    val populateBookDetailsEventJpaRepository: PopulateBookDetailsEventJpaRepository,
    val syncBookEventJpaRepository: SyncBookEventJpaRepository,
    val populateBookBasicDataJpaRepository: PopulateBookBasicDataJpaRepository
) {

    /*fun getUnprocessedEvents(eventClass: Class<*>): List<Any> {
        return when (eventClass) {
            PopulateBookDetailsEventJpaRepository::class.java -> populateBookDetailsEventJpaRepository.findAllPending()
            SyncBookEventJpaRepository::class.java -> syncBookEventJpaRepository.findAllPending()
            PopulateBookBasicDataJpaRepository::class.java -> populateBookBasicDataJpaRepository.findAllPending()
            else -> throw IllegalArgumentException("Unsupported event class: ${eventClass.name}")
        }

    }*/
}