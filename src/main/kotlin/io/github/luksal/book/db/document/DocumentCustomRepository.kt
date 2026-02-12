package io.github.luksal.book.db.document

import com.mongodb.bulk.BulkWriteUpsert

interface DocumentCustomRepository<T> {
    fun saveBulkWithDeduplication(docs: List<T>): List<BulkWriteUpsert>
}