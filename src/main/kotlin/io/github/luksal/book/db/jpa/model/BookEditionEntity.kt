package io.github.luksal.book.db.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "book_editions")
data class BookEditionEntity(

    @Id
    @Column
    private val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val language: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    val book: BookEntity,
)
