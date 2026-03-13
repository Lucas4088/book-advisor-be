package io.github.luksal.book.api.dto

data class AuthorSearchCriteria(
    val id: Long? = null,
    val publicId: String? = null,
    val name: String? = null
)

data class SearchAuthorsResponse(
    val authors: List<AuthorDto>
)

data class AuthorDto(
    val id: Long? = null,
    val publicId: String,
    val name: String? = null
)

data class AuthorDetailsDto(
    val id: Long? = null,
    val publicId: String,
    val name: String? = null
)