package io.github.luksal.book.service

import io.github.luksal.book.api.dto.AuthorDetailsDto
import io.github.luksal.book.api.dto.AuthorDto
import io.github.luksal.book.api.dto.AuthorSearchCriteria
import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.book.db.document.author.repository.AuthorDocumentRepository
import io.github.luksal.book.db.jpa.AuthorJpaRepository
import io.github.luksal.book.mapper.BookMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AuthorService(private val authorDocumentRepository: AuthorDocumentRepository,
                    private val authorJpaRepository: AuthorJpaRepository) {

    fun saveAuthorDocument(authorDocument: AuthorDocument) =
        authorDocumentRepository.save(authorDocument)

    fun saveAuthorDocuments(authorDocuments: List<AuthorDocument>) =
        authorDocumentRepository.saveAll(authorDocuments)

    fun getAuthors(authorsKeys: List<String>): List<AuthorDocument> =
        authorDocumentRepository.findAllById(authorsKeys)

    fun searchAuthors(criteria: AuthorSearchCriteria, page: Pageable) : Page<AuthorDto> =
        authorJpaRepository.search(criteria.id, criteria.publicId, criteria.name, page).map(BookMapper::map)

    fun getAuthorDetails(publicId: String) : AuthorDetailsDto? =
        authorJpaRepository.findByPublicId(publicId)?.let {
            BookMapper.mapDetails(it)
        }
}