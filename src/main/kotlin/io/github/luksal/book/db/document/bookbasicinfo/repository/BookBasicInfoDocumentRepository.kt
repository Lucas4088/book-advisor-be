package io.github.luksal.book.db.document.bookbasicinfo.repository

import io.github.luksal.book.db.document.DocumentCustomRepository
import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import io.github.luksal.commons.jpa.MongoCustomRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookBasicInfoDocumentRepository : MongoRepository<BookBasicInfoDocument, String>,
    MongoCustomRepository<BookBasicInfoDocument>,
    BookBasicInfoDocumentCustomRepository<BookBasicInfoDocument>,
    DocumentCustomRepository<BookBasicInfoDocument> {
    fun findAllByBookPublicIdIn(bookIds: List<String>, page: Pageable): Page<BookBasicInfoDocument>

}