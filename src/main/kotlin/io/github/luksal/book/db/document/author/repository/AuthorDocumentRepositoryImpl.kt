package io.github.luksal.book.db.document.author.repository

import io.github.luksal.book.db.document.author.AuthorDocument
import io.github.luksal.commons.jpa.MongoCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class AuthorDocumentRepositoryImpl(private val template: MongoTemplate) : MongoCustomRepository<AuthorDocument> {
    override fun countApprox(): Long =
        template.estimatedCount(AuthorDocument::class.java)

}