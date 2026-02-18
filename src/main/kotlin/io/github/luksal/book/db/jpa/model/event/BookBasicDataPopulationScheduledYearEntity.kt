package io.github.luksal.book.db.jpa.model.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "book_basic_data_population_scheduled_years")
class BookBasicDataPopulationScheduledYearEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val year: Int,

    @Column(nullable = false)
    val lang: String,

    @Column(nullable = false)
    var processed: Boolean = false,

    @Column(nullable = false)
    val timestamp: Long
)