package io.github.luksal.book.db.document

import io.github.luksal.book.db.document.model.BookBasicInfoDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookBasicInfoRepository : MongoRepository<BookBasicInfoDocument, String>