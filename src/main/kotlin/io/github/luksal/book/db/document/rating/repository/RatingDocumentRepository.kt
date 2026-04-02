package io.github.luksal.book.db.document.rating.repository

import io.github.luksal.book.db.document.book.BookRatingCountStats
import io.github.luksal.book.db.document.book.RatingDocument
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingDocumentRepository : MongoRepository<RatingDocument, String> {

    @Aggregation(
        pipeline = [
            // Step 1: count ratings per book
            $$"{ '$group': { '_id': '$bookId', 'ratingCount': { '$sum': 1 } } }",
            // Step 2: group by ratingCount
            $$"{ '$group': { '_id': '$ratingCount', 'documents': { '$sum': 1 } } }",
            // Step 3: reshape output
            $$"{ '$project': { '_id': 0, 'ratingCount': '$_id', 'documents': 1 } }",
            // Step 4: sort nicely
            $$"{ '$sort': { 'ratingCount': 1 } }"
        ]
    )
    fun countBooksPerRatingNumber(): List<BookRatingCountStats>
}