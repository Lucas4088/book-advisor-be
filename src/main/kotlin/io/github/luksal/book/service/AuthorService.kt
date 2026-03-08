package io.github.luksal.book.service

import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.author.repository.AuthorDocumentRepository
import org.springframework.stereotype.Service

@Service
class AuthorService(private val authorDocumentRepository: AuthorDocumentRepository) {

    fun saveAuthorDocument(authorDocument: AuthorDocument)
    = authorDocumentRepository.save(authorDocument)

    fun saveAuthorDocuments(authorDocuments: List<AuthorDocument>)
            = authorDocumentRepository.saveAll(authorDocuments)

    fun getAuthors(authorsKeys: List<String>): List<AuthorDocument>
    = authorDocumentRepository.findAllById(authorsKeys)

}