package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.db.jpa.model.BookWithScoreProjection
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
     SELECT * FROM (       
        SELECT 
            b.id AS bookId,
            b.title AS title,
            b.description AS description,
            b.small_thumbnail_url AS smallThumbnailUrl,
            COALESCE((
                    SELECT SUM(r2.score * r2.count)
                    FROM ratings r2
                    WHERE r2.book_id = b.id
                ), 0)
                /
                NULLIF((
                    SELECT SUM(r3.count)
                    FROM ratings r3
                    WHERE r3.book_id = b.id
                ), 0)
                AS ratingScore,
            COALESCE((
                SELECT SUM(r3.count)
                FROM ratings r3
                WHERE r3.book_id = b.id
            ), 0) AS ratingCount
        FROM books b
        LEFT JOIN book_genres bg ON b.id = bg.book_id
        LEFT JOIN genres g ON g.id = bg.genre_id
        WHERE 
            (:bookId IS NULL OR b.id LIKE CONCAT('%', :bookId, '%')) AND
            (:title IS NULL OR LOWER(b.title) LIKE CONCAT('%', LOWER(:title), '%')) AND
            (:startYear IS NULL OR b.publishing_year >= :startYear) AND
            (:endYear IS NULL OR b.publishing_year <= :endYear) AND
             (
                :genresSize is null or :genresSize = 0 OR g.name IN (:genres)
              )
        GROUP BY b.id, b.title, b.small_thumbnail_url
        HAVING (
            COALESCE(:genresSize, -1) = -1 OR
            COUNT(DISTINCT g.name) = COALESCE(:genresSize, COUNT(DISTINCT g.name))
        )) as t
    ORDER BY t.ratingScore * LN(1 + ratingCount) DESC NULLS LAST
    """,
        countQuery = """
    SELECT COUNT(*) FROM (
        SELECT b.id
        FROM books b
        LEFT JOIN book_genres bg ON b.id = bg.book_id
        LEFT JOIN genres g ON g.id = bg.genre_id
        WHERE 
            (:bookId IS NULL OR b.id LIKE CONCAT('%', :bookId, '%')) AND
            (:title IS NULL OR LOWER(b.title) LIKE CONCAT('%', LOWER(:title), '%')) AND
            (:startYear IS NULL OR b.publishing_year >= :startYear) AND
            (:endYear IS NULL OR b.publishing_year <= :endYear) AND
           (
              :genresSize is null or :genresSize = 0 OR g.name IN (:genres)
            )
        GROUP BY b.id
        HAVING (
            COALESCE(:genresSize, -1) = -1 OR
            COUNT(DISTINCT g.name) = COALESCE(:genresSize, COUNT(DISTINCT g.name))
        )
    ) AS counted
    """,
        nativeQuery = true
    )
    fun search(
        @Param("bookId") bookId: String? = null,
        @Param("title") title: String? = null,
        @Param("startYear") startYear: Int? = null,
        @Param("endYear") endYear: Int? = null,
        @Param("genres") genres: List<String>? = null,
        @Param("genresSize") genresSize: Int? = null,
        pageable: Pageable
    ): Page<BookWithScoreProjection>


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