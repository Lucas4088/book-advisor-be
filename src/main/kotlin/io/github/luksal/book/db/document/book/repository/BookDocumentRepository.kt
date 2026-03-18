package io.github.luksal.book.db.document.book.repository

import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.BookRatingCountStats
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookDocumentRepository : MongoRepository<BookDocument, String>, BookDocumentCustomRepository,
    DocumentCustomRepository<BookDocument> {

    @Aggregation(
        pipeline = [
            "{ '\$unwind': '\$ra' }",
            "{ '\$group': { '_id': '\$_id', 'count': { '\$sum': 1 } } }",
            "{ '\$group': { '_id': '\$count', 'documents': { '\$sum': 1 } } }",
            "{ '\$project': { '_id': 0, 'ratingCount': '\$_id', 'documents': 1 } }",
            "{ '\$sort': { 'ratingCount': -1 } }"
        ]
    )
    fun countBooksPerRatingNumber(): List<BookRatingCountStats>
}