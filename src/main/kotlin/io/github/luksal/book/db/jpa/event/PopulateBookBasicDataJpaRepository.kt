package io.github.luksal.book.db.jpa.event

import io.github.luksal.book.common.jpa.event.EventStatus
import io.github.luksal.book.db.jpa.model.event.ScheduledBookBasicInfoPopulationEventEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PopulateBookBasicDataJpaRepository : JpaRepository<ScheduledBookBasicInfoPopulationEventEntity, Long> {
    fun findFirstByMeta_Status(status: EventStatus): ScheduledBookBasicInfoPopulationEventEntity?
    fun existsByYearAndLangAndMeta_Status(year: Int, lang: String, status: EventStatus): Boolean

    @Query("""
        select e from ScheduledBookBasicInfoPopulationEventEntity e
        where (:yearFrom is null or e.year >= :yearFrom) 
        and (:yearTo is null or e.year <= :yearTo) 
        and (:lang is null or e.lang like %:lang%) 
        and (:status is null or e.meta.status in :status) 
    """)
    fun searchAll(yearFrom: Int?, yearTo: Int?, lang: String?, status: List<EventStatus>?, page: Pageable) : Page<ScheduledBookBasicInfoPopulationEventEntity>
}