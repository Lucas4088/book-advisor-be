package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.AuthorEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorJpaRepository : JpaRepository<AuthorEntity, Long> {
    fun findByPublicId(publicId: String): AuthorEntity?
}