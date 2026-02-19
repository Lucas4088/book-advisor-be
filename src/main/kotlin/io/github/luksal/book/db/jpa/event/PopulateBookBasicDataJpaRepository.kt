package io.github.luksal.book.db.jpa.event

import io.github.luksal.book.common.jpa.event.EventStatus
import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PopulateBookBasicDataJpaRepository : JpaRepository<ScheduledBookBasicInfoPopulationEventEntity, Long> {
    fun findFirstByMeta_Status(status: EventStatus): ScheduledBookBasicInfoPopulationEventEntity?
    fun existsByYearAndLangAndMeta_Status(year: Int, lang: String, status: EventStatus): Boolean
}