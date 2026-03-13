package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.AuthorEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AuthorJpaRepository : JpaRepository<AuthorEntity, Long> {
    fun findByPublicId(publicId: String): AuthorEntity?

    @Query(
        """
            select p from AuthorEntity p where 
             (:id is null or :id = p.id) and
             (:publicId is null or  p.publicId like %:publicId%) and
             (:name is null or p.name like %:name%)"""
    )
    fun search(
        @Param("id") id: Long?,
        @Param("publicId") publicId: String?,
        @Param("name") name: String?,
        page: Pageable
    ): Page<AuthorEntity>
}