package io.github.luksal.integration.source.archivebooks.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArchiveBookDetailsResponse(
    val metadata: Metadata,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Metadata(
    val identifier: String?,
    val title: String,
    val creator: String?,
    val publisher: String?,
    val date: String?,
    val language: String?,
    val description: List<String>?,
    val isbn: List<String>?,
    val subject: List<String>?,
    val mediatype: String?,
    val collection: List<String>?,
    val publicdate: String?,
    val imagecount: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArchiveSearchResponse(
    val response: SearchResponse?
)


@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchResponse(
    val numFound: Int,
    val start: Int,
    val docs: List<ArchiveSearchDoc>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArchiveSearchDoc(
    val identifier: String,
    val title: String?,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val creator: List<String>?,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val date: List<String>?,
    val year: Int?,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val language: List<String>?,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val publisher: List<String>?,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val description: List<String>?,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val subject: List<String>?,
    val collection: List<String>?,
    val item_size: Long?,
)