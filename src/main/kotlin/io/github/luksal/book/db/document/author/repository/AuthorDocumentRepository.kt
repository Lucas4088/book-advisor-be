package io.github.luksal.book.db.document.author.repository

import io.github.luksal.book.db.document.author.AuthorDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorDocumentRepository : MongoRepository<AuthorDocument, String> {
}