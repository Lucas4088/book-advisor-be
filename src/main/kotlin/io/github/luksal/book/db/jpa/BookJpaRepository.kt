package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.BookEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BookJpaRepository : JpaRepository<BookEntity, Long> {

    @Query("""
        select b from BookEntity b 
        where (b.title is null or b.title like %:title%) 
           and b.publishingYear >= :startYear
           and b.publishingYear <= :endYear
           and (:genres is null or :genres member of b.genres)
    """)
    fun searchAll(title: String?, startYear: Int, endYear: Int, genres: List<String>, pageable: Pageable): Page<BookEntity>
}