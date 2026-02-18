package io.github.luksal.book.service

import org.springframework.stereotype.Service

@Service
class PopulateBookDataService(
    private val bookDateService: SyncBookDataService
) {
}