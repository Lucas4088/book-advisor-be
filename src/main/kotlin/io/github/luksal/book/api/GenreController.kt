package io.github.luksal.book.api

import io.github.luksal.book.service.GenreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/genre")
class GenreController(private val genreService: GenreService) {

    @GetMapping
    fun findAll() = genreService.findAll()

}