package io.github.luksal.integration.db

import io.github.luksal.statistics.api.dto.BookDetailsFetchedValueDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BookDetailsFetchedEventRepository : JpaRepository<BookDetailsFetchedEventEntity, Int> {

    @Query(value = """
                 SELECT
                    bucket,
                    e.source_name source_name,
                    e.status status,
                    count(e.id)
                FROM generate_series(
                    now() - interval '30 minutes',
                    now(),
                    interval '6 minute'
                ) AS t(bucket)
                LEFT JOIN book_details_fetched_events e
                    ON e.created_at <= bucket AND e.created_at >= date_trunc('day', now())
                GROUP BY bucket, e.source_name, e.status
                ORDER BY bucket;
                """, nativeQuery = true)
    fun countBySource(): List<BookDetailsFetchedValueDto>
}