package io.github.luksal.book.db.jpa.model

import io.github.luksal.book.db.jpa.util.StringListToStringConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "authors")
class AuthorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val publicId: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    @Convert(converter = StringListToStringConverter::class)
    val otherNames: List<String>? = null
)