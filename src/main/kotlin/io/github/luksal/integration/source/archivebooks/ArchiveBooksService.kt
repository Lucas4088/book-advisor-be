package io.github.luksal.integration.source.archivebooks

import io.github.luksal.integration.source.archivebooks.api.ArchiveBooksClient
import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveBookDetailsResponse
import io.github.luksal.integration.source.archivebooks.api.dto.ArchiveSearchDoc
import io.github.luksal.util.ext.logger
import io.github.luksal.util.ext.normalizeStandardChars
import org.springframework.stereotype.Service

@Service
class ArchiveBooksService(private val archiveBooksClient: ArchiveBooksClient) {

    private val log = logger()

    companion object {
        private const val TITLE_QUERY_PARAM = "title"
        private const val CREATOR_QUERY_PARAM = "creator"
        private const val LANG_QUERY_PARAM = "language"
        private const val MEDIATYPE_QUERY_PARAM = "mediatype"
    }

    fun search(title: String, author: String?): List<ArchiveSearchDoc> {
        val authorQueryPart = author?.let { "AND $CREATOR_QUERY_PARAM:(\"${author.normalizeStandardChars()}\")" } ?: ""
        val langPart = "AND ($LANG_QUERY_PARAM:eng OR $LANG_QUERY_PARAM:English)"
        val query = "$TITLE_QUERY_PARAM:\"$title\" $authorQueryPart $langPart AND $MEDIATYPE_QUERY_PARAM:texts"
        return archiveBooksClient.search(query)?.response?.docs?.also {
            log.info("Found ${it.size} search results")
        } ?: emptyList()
    }

    fun findById(id: String): ArchiveBookDetailsResponse? {
        return archiveBooksClient.findById(id)
    }
}