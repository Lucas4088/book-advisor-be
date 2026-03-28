package io.github.luksal.book.db.jpa.model

import jakarta.persistence.*
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "books")
class BookEntity(
    @Id
    @Column(name = "id")
    val bookId: String? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column
    val language: String?,

    @Column(nullable = false)
    val publishingYear: Int,

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
    var ratings: MutableList<RatingEntity> = mutableListOf(),

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    var editions: MutableList<BookEditionEntity> = mutableListOf(),

    @Version
    var version: Int? = null
) : Persistable<String> {
    @Transient
    private var isNew = true

    override fun getId(): String? = bookId

    override fun isNew(): Boolean = isNew

}