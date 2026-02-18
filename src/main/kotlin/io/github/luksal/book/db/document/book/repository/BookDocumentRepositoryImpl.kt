package io.github.luksal.book.db.document.book.repository

import com.mongodb.bulk.BulkWriteUpsert
import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.RatingEmbedded
import io.github.luksal.book.mapper.BookMapper
import io.github.luksal.book.model.BookUpdate
import io.github.luksal.util.ext.pushIfNotEmpty
import io.github.luksal.util.ext.setIfNotEmpty
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class BookDocumentRepositoryImpl(
    private val mongoOps: MongoOperations,
    private val mongoTemplate: MongoTemplate
) : DocumentCustomRepository<BookDocument>, BookDocumentCustomRepository {

    //check if generic will work for both BookDocument and BookBasicInfoDocument, if not - create separate impl for BookBasicInfoDocument
    override fun saveBulkWithDeduplication(docs: List<BookDocument>): List<BulkWriteUpsert> {
        val bulkOps = mongoOps.bulkOps(BulkOperations.BulkMode.UNORDERED, BookDocument::class.java)
        docs.forEach { book ->
            bulkOps.replaceOne(
                Query.query(Criteria.where("_id").`is`(book.id)),
                book,
                FindAndReplaceOptions.options().upsert()
            )
        }
        return bulkOps.execute().upserts
    }

    override fun search(
        title: String?, startYear: Int, endYear: Int,
        genres: List<String>?, pageable: Pageable
    ): Page<BookDocument> {
        val criteria = mutableListOf<Criteria>()

        title?.takeIf { it.isNotBlank() }?.let {
            criteria += Criteria.where("title").regex(it, "i")
        }
        startYear.let {
            criteria += Criteria.where("publishingYear").gte(it)
        }
        endYear.let {
            criteria += Criteria.where("publishingYear").lte(it)
        }
        genres?.takeIf { it.isNotEmpty() }?.let {
            criteria += Criteria.where("genres").`in`(it)
        }

        val query = Query().apply {
            if (criteria.isNotEmpty()) {
                addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
            }
            with(pageable)
        }

        val books = mongoTemplate.find(query, BookDocument::class.java)
        val count = mongoTemplate.count(query, BookDocument::class.java)
        return PageImpl(books, pageable, count)
    }

    override fun update(book: BookUpdate): String? {
        val update = Update().apply {
            BookMapper.map(book).let { document ->
                setIfNotEmpty("description", document.description)
                setIfNotEmpty("title", document.title)
                setIfNotEmpty("publishingYear", document.publishingYear)
                setIfNotEmpty("pageCount", document.pageCount)
                setIfNotEmpty("thumbnailUrl", document.thumbnailUrl)
                setIfNotEmpty("smallThumbnailUrl", document.smallThumbnailUrl)
                setIfNotEmpty("edition", document.edition)
                pushIfNotEmpty("authors", document.authors)
                pushIfNotEmpty("genres", document.genres)
                pushIfNotEmpty("ratings", document.ratings)
            }
        }
        return mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(book.id)),
            update,
            BookDocument::class.java
        ).upsertedId?.toString()
    }
}