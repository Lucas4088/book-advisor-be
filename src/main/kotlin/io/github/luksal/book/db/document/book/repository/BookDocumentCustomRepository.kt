package io.github.luksal.book.db.document.book.repository

import io.github.luksal.book.db.document.book.BookDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDocumentCustomRepository {
    fun search(title: String?, startYear: Int, endYear: Int, genres: List<String>?, pageable: Pageable): Page<BookDocument>
}