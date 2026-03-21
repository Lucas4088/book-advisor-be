package io.github.luksal.book.db.document.bookbasicinfo.repository

import com.mongodb.bulk.BulkWriteUpsert
import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.commons.jpa.MongoCustomRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class BookBasicInfoDocumentRepositoryImpl(
    private val mongoOps: MongoOperations,
    private val mongoTemplate: MongoTemplate
) : DocumentCustomRepository<BookBasicInfoDocument>, BookBasicInfoDocumentCustomRepository<BookBasicInfoDocument>,
    MongoCustomRepository<BookBasicInfoDocument> {

    override fun saveBulkWithDeduplication(docs: List<BookBasicInfoDocument>): List<BulkWriteUpsert> {
        val bulkOps = mongoOps.bulkOps(BulkOperations.BulkMode.UNORDERED, BookBasicInfoDocument::class.java)
        docs.forEach { info ->
            bulkOps.replaceOne(
                Query.query(Criteria.where("pbi").`is`(info.bookPublicId)),
                info,
                FindAndReplaceOptions.options().upsert()
            )
        }
        return bulkOps.execute().upserts
    }

    override fun search(
        id: Long?,
        bookId: String?,
        title: String?,
        startYear: String?,
        endYear: String?,
        pageable: Pageable
    ): Page<BookBasicInfoDocument> {
        val criteria = mutableListOf<Criteria>()

        id?.let { criteria += Criteria.where("id").`is`(it) }

        bookId?.let { criteria += Criteria.where("bookPublicId").regex(it, "i") }

        title?.takeIf { it.isNotBlank() }?.let {
            criteria += Criteria.where($$"$text").`is`(mapOf($$"$search" to it))
        }
  /*      startYear.let {
            criteria += Criteria.where("firstPublishDate").gte(it)
        }
        endYear.let {
            criteria += Criteria.where("firstPublishDate").lte(it)
        }*/

        val query = Query().apply {
            if (criteria.isNotEmpty()) {
                addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
            }
        }

        val countQuery = Query().apply {
            if (criteria.isNotEmpty()) {
                addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
            }
        }

        query.with(pageable)
        mongoTemplate.useEstimatedCount(true)
        val count = mongoTemplate.count(countQuery, BookBasicInfoDocument::class.java)
        val books = mongoTemplate.find(query, BookBasicInfoDocument::class.java)
        return PageImpl(books, pageable, count)
    }

    override fun countApprox(): Long = mongoTemplate.estimatedCount(BookBasicInfoDocument::class.java)
}