package io.github.luksal.book.api

import io.github.luksal.book.api.dto.AuthorDetailsDto
import io.github.luksal.book.api.dto.AuthorDto
import io.github.luksal.book.api.dto.AuthorSearchCriteria
import io.github.luksal.book.service.AuthorService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/author")
class AuthorController(
    private val authorService: AuthorService
) {

    @PostMapping
    fun search(@RequestBody criteria: AuthorSearchCriteria, page: Pageable): Page<AuthorDto> =
        authorService.searchAuthors(criteria, page)

    @GetMapping("/{publicId}")
    fun getById(@PathVariable publicId: String): AuthorDetailsDto? =
        authorService.getAuthorDetails(publicId)

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String) = {}
}