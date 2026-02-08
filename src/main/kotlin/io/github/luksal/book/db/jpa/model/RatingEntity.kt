package io.github.luksal.book.db.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "ratings")
class RatingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    val book: BookEntity,

    @Column(precision = 3, scale = 2)
    val rating: BigDecimal,

    val count: Int,

    @ManyToOne
    @JoinColumn(name = "source_id")
    val source: RatingSourceEntity
)
