package io.github.luksal.book.db.jpa.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "ratings")
class RatingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    val book: BookEntity,

    @Column(precision = 3, scale = 2, nullable = false)
    val score: BigDecimal,

    val count: Int?,

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    val source: RatingSourceEntity,

    @Column(precision = 3, scale = 2, nullable = false)
    val titleConfidenceIndicator: BigDecimal,

    @Column(precision = 3, scale = 2, nullable = false)
    val authorsConfidenceIndicator: BigDecimal,
)
