package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.BookEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookJpaRepository : JpaRepository<BookEntity, String> {

    @Query(
        """
        select distinct b from BookEntity b 
        left join b.genres g
        where (b.title is null or b.title like %:title%) 
           and (:startYear is null or b.publishingYear >= :startYear)
           and (:endYear is null or b.publishingYear <= :endYear)
           and (:#{#genres == null || #genres.empty} = true or g.name in :genres)
    """
    )
    fun searchAll(
        title: String?,
        startYear: Int?,
        endYear: Int?,
        genres: List<String>?,
        pageable: Pageable
    ): Page<BookEntity>

    @Query(
        """
        select distinct b from BookEntity b 
        left join b.genres g where 
        (:bookId is null or b.bookId like %:bookId%) and
        (:title is null or lower(b.title) like %:title%) and
        (:startYear is null or b.publishingYear >= :startYear) and
        (:endYear is null or b.publishingYear <= :endYear) and 
        (:#{#genres == null || #genres.empty} = true or g.name in :genres)
        group by b.id
        having (:#{#genres == null || #genres.empty} = true 
                or count(distinct g.name) = :#{#genres == null ? 0 : #genres.size()})
    """
    )
    fun search(
        bookId: String? = null,
        title: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        genres: List<String>? = null,
        pageable: Pageable
    ): Page<BookEntity>


    @Query("""
         SELECT DISTINCT b.*, 
            array_agg(DISTINCT g.name) as genres,
            array_agg(DISTINCT t.name) as tags
        FROM books b
        JOIN book_genres bg ON b.id = bg.book_id
        JOIN genres g ON bg.genre_id = g.id
        LEFT JOIN book_tags bt ON b.id = bt.book_id
        LEFT JOIN tags t ON bt.tag_id = t.id
        WHERE b.id NOT IN (
            SELECT bg2.book_id FROM book_genres bg2
            JOIN genres g2 ON bg2.genre_id = g2.id
            WHERE g2.name IN :allowed_genres
        )
        GROUP BY b.id
    """, nativeQuery = true)
    fun getBookForGenreClassification(@Param("allowed_genres") allowedGenres: List<String>): List<BookEntity>
}