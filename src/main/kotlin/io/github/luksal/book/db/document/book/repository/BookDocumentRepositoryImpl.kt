package io.github.luksal.book.db.document.book.repository

import com.mongodb.bulk.BulkWriteUpsert
import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.RatingEmbedded
import io.github.luksal.book.model.Book
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
import org.springframework.data.mongodb.core.query.UpdateDefinition
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

    override fun updateRating(bookId: String, newValue: RatingEmbedded): String? =
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(bookId)),
            Update().addToSet("ratings", newValue),
            BookDocument::class.java
        ).upsertedId?.toString()

    override fun updateDescription(bookId: String, newValue: String): String? =
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(bookId)),
            Update().set("description", newValue),
            BookDocument::class.java
        ).upsertedId?.toString()

    override fun update(book: Book): String? {
        var update: UpdateDefinition
     /*   book.title.let { update.set("title", it) }
        book.description.let { update.set("description", it) }
        book.publishingYear?.let { update.set("publishingYear", it) }
        book.pageCount.let { update.set("pageCount", it) }
        book.thumbnailUrl.let { update.set("thumbnailUrl", it) }
        book.smallThumbnailUrl.let { update.set("smallThumbnailUrl", it) }

        book.edition?.let { update.set("edition", it) }
        book.authors?.let { update.set("authors", it) }
        book.genres?.let { update.set("genres", it) }
        book.ratings?.let { update.set("ratings", it) }*/

        return mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(book.id)),
            Update().set("description", book.description),
            BookDocument::class.java
        ).upsertedId?.toString()
    }

}