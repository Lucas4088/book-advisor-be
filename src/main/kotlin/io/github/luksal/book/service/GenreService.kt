package io.github.luksal.book.service

import io.github.luksal.book.db.jpa.GenreJpaRepository
import org.springframework.stereotype.Service

@Service
class GenreService(private val repository: GenreJpaRepository) {

    fun findAll(): List<String> = repository.findAll().map { it.name }

}