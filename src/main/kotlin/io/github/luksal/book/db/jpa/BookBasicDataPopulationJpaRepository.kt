package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.event.BookBasicDataPopulationScheduledYearEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookBasicDataPopulationJpaRepository : JpaRepository<BookBasicDataPopulationScheduledYearEntity, Long> {

    fun findFirstByProcessedIsFalse(): BookBasicDataPopulationScheduledYearEntity?
    fun existsByYearAndLangAndProcessed(year: Int, lang: String, processed: Boolean): Boolean
}