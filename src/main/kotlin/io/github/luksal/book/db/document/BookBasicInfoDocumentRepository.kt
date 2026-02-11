package io.github.luksal.book.db.document

import io.github.luksal.book.db.document.model.BookBasicInfoDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookBasicInfoDocumentRepository : MongoRepository<BookBasicInfoDocument, String> {
    fun findByProcessed(processed: Boolean, page: Pageable): Page<BookBasicInfoDocument>
}