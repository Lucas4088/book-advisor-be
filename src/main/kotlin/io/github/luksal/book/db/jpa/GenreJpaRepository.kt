package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.GenreEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GenreJpaRepository : JpaRepository<GenreEntity, Long> {
    fun findByName(name: String): GenreEntity?
}