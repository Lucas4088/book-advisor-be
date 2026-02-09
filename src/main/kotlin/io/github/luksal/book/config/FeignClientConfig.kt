package io.github.luksal.book.config

import io.github.luksal.book.openlibrary.api.OpenLibraryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(clients = [OpenLibraryClient::class])
class FeignClientConfig