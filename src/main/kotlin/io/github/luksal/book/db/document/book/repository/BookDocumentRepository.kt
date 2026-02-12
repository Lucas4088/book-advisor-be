package io.github.luksal.book.db.document.book.repository

import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.book.BookDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookDocumentRepository : MongoRepository<BookDocument, String>, BookDocumentCustomRepository, DocumentCustomRepository<BookDocument>