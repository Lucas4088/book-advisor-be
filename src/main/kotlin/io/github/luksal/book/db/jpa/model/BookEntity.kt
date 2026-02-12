package io.github.luksal.book.db.jpa.model

import io.github.luksal.book.api.dto.BookSearchResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "books")
class BookEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: String? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String,

    @Column(nullable = false)
    val publishingYear: Int,

    val pageCount: Int,

    val thumbnailUrl: String,
    val smallThumbnailUrl: String,

    @ManyToMany
    @JoinTable(
        name = "book_authors",
        joinColumns = [JoinColumn(name = "book_id")],
        inverseJoinColumns = [JoinColumn(name = "author_id")]
    )
    val authors: MutableSet<AuthorEntity> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "book_genres",
        joinColumns = [JoinColumn(name = "book_id")],
        inverseJoinColumns = [JoinColumn(name = "genre_id")]
    )
    val genres: MutableSet<GenreEntity> = mutableSetOf(),

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ratings: MutableList<RatingEntity> = mutableListOf()
) {
    companion object {
        fun toSearchResponse(book: BookEntity): BookSearchResponse = BookSearchResponse(
            id = book.id,
            title = book.title,
            smallThumbnailUrl = book.smallThumbnailUrl
        )
    }
}