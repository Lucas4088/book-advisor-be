package io.github.luksal.book.db.jpa.model

import jakarta.persistence.*

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
    val timestamp: Long?
)