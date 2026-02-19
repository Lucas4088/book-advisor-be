package io.github.luksal.book.db.jpa.event

import io.github.luksal.book.db.jpa.model.event.PopulateBookDetailsEventEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PopulateBookDetailsEventJpaRepository : JpaRepository<PopulateBookDetailsEventEntity, Long> {

    @Query("SELECT e FROM PopulateBookDetailsEventEntity e WHERE e.meta.status = 'PENDING' ORDER BY e.meta.createdAt ASC")
    fun findAllPending(page: Pageable): Page<PopulateBookDetailsEventEntity>
}