package io.github.luksal.book.db.document.bookbasicinfo.repository

import io.github.luksal.book.db.document.bookbasicinfo.BookBasicInfoDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookBasicInfoDocumentCustomRepository<T> {
    fun search(
        id: Long?,
        bookId: String?,
        title: String?,
        startYear: String?,
        endYear: String?,
        pageable: Pageable
    ): Page<BookBasicInfoDocument>
}