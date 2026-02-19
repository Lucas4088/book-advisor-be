package io.github.luksal.book.db.jpa.model

import jakarta.persistence.*

@Entity
@Table(name = "books")
class BookEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: String? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column(nullable = false)
    val publishingYear: Int?,

    val pageCount: Int?,

    val thumbnailUrl: String?,
    val smallThumbnailUrl: String?,

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
    var ratings: MutableList<RatingEntity> = mutableListOf()
)