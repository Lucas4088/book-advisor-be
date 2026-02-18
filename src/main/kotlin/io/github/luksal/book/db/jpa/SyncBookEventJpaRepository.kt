package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.event.SyncBookEventEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SyncBookEventJpaRepository : JpaRepository<SyncBookEventEntity, Long> {

    @Query("SELECT e FROM SyncBookEventEntity e WHERE e.processed = false ORDER BY e.timestamp ASC")
    fun findAllByProcessedFalse(page: Pageable): Page<SyncBookEventEntity>
}