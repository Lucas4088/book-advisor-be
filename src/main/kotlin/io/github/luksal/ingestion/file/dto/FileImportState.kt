package io.github.luksal.ingestion.file.dto

data class FileImportState(
    val filePointer: Long = 0,
    val formattedProgress: String? = null
)