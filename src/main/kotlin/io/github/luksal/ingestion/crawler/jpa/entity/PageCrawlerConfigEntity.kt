package io.github.luksal.ingestion.crawler.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "crawler_configs")
data class PageCrawlerConfigEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @Column(unique = true, nullable = false)
    val name: String,

    @Column(nullable = false)
    val enabled: Boolean,

    @Column(unique = true, nullable = false)
    val baseUrl: String,

    @Embedded
    val rateLimit: RateLimit,

    @Embedded
    val path: CrawlerPath,

    @Column(nullable = false)
    val proxyEnabled: Boolean
)

@Embeddable
data class RateLimit(
    @Column(nullable = false)
    val requestsPerMinute: Int,

    @Column(nullable = false)
    val burst: Int
)

@Embeddable
data class CrawlerPath(
    @Column(nullable = false)
    val bookResultSelector: String,

    @Column(nullable = false)
    val bookRatingCountSelector: String,

    @Column(nullable = false)
    val bookRatingScoreSelector: String,

    @Column(nullable = false)
    val search: String,

    @Column(nullable = false)
    val titleSpaceSeparator: String
)