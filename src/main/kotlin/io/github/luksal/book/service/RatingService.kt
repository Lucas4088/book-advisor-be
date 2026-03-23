package io.github.luksal.book.service

import io.github.luksal.book.db.document.book.RatingDocument
import io.github.luksal.book.db.jpa.BookJpaRepository
import io.github.luksal.book.db.jpa.RatingJpaRepository
import io.github.luksal.book.db.jpa.RatingSourceJpaRepository
import io.github.luksal.book.db.jpa.model.RatingSourceEntity
import io.github.luksal.book.mapper.BookMapper.mapToEntity
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RatingService(
    private val ratingJpaRepository: RatingJpaRepository,
    private val ratingSourceJpaRepository: RatingSourceJpaRepository,
    private val bookJpaRepository: BookJpaRepository
) {

    @Transactional
    fun syncToEntity(document: RatingDocument) {
        val source = ratingSourceJpaRepository.findByName(document.source.name)
            ?: ratingSourceJpaRepository.save(
                RatingSourceEntity(
                    name = document.source.name,
                    url = document.source.url,
                )
            )

        val book = bookJpaRepository.findById(document.bookId)
            .orElseThrow()
        val ratingEntity = ratingJpaRepository.findByBookIdAndSourceId(book.bookId!!, source.id!!)
            .map {
                it.score = document.score
                it.count = document.count
                it
            }.orElseGet { document.mapToEntity(book, source) }

        ratingJpaRepository.save(ratingEntity)
    }
}