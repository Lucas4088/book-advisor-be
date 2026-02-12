package io.github.luksal.book.db.document.bookbasicinfo.repository

import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookBasicInfoDocumentRepository : MongoRepository<BookBasicInfoDocument, String>,
    DocumentCustomRepository<BookBasicInfoDocument> {
    fun findByProcessed(processed: Boolean, page: Pageable): Page<BookBasicInfoDocument>
}