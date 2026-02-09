package io.github.luksal.book.db.document

import io.github.luksal.book.db.document.model.BookDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookDocumentRepository : MongoRepository<BookDocument, String> {}