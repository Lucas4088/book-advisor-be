package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.document.book.BookRatingCountStats
import io.github.luksal.book.db.jpa.model.RatingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RatingJpaRepository : JpaRepository<RatingEntity, Long> {

    fun findByBookIdAndSourceId(bookId: String, sourceId: Int): Optional<RatingEntity>


    @Query(value = """
            SELECT rating_count AS ratingCount, COUNT(*) AS documents
            FROM (
                SELECT r.book_id, COUNT(r.id) AS rating_count
                FROM ratings r
                GROUP BY r.book_id
            ) t
            GROUP BY rating_count
    """, nativeQuery = true)
    fun countBooksPerRatingNumber(): List<BookRatingCountStats>
}