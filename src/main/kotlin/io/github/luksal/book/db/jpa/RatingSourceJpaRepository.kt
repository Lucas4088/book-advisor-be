package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.RatingSourceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingSourceJpaRepository : JpaRepository<RatingSourceEntity, Long> {
    fun findByName(name: String): RatingSourceEntity?
}