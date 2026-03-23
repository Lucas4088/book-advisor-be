package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.RatingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RatingJpaRepository : JpaRepository<RatingEntity, Long> {

    fun findByBookIdAndSourceId(bookId: String, sourceId: Int): Optional<RatingEntity>
}