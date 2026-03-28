package io.github.luksal.book.db.jpa

import io.github.luksal.book.db.jpa.model.BookEditionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookEditionJpaRepository : JpaRepository<BookEditionEntity, Long>