package io.github.luksal.book.db.document.rating.repository

import io.github.luksal.book.db.document.book.RatingDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingDocumentRepository : MongoRepository<RatingDocument, String> {
}