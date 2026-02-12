package io.github.luksal.book.db.document.bookbasicinfo.repository

import com.mongodb.bulk.BulkWriteUpsert
import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class BookBasicInfoDocumentRepositoryImpl(private val mongoOps: MongoOperations) :
    DocumentCustomRepository<BookBasicInfoDocument> {

    override fun saveBulkWithDeduplication(docs: List<BookBasicInfoDocument>): List<BulkWriteUpsert>{
        val bulkOps = mongoOps.bulkOps(BulkOperations.BulkMode.UNORDERED, BookBasicInfoDocument::class.java)
        docs.forEach { info ->
            bulkOps.replaceOne(
                Query.query(Criteria.where("_id").`is`(info.id)),
                info,
                FindAndReplaceOptions.options().upsert()
            )
        }
        return bulkOps.execute().upserts
    }
}