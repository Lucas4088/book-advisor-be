package io.github.luksal.book.db.jpa.model

import jakarta.persistence.*

@Entity
@Table(name = "tags")
class TagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,
)