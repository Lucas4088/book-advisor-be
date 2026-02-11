package io.github.luksal.book.config

import feign.FeignException
import feign.RequestInterceptor
import feign.Response
import feign.RetryableException
import feign.Retryer
import feign.codec.ErrorDecoder
import io.github.luksal.book.ext.logger
import io.github.luksal.book.googlebooks.api.GoogleBooksClient
import io.github.luksal.book.openlibrary.api.OpenLibraryClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import java.io.IOException
import java.nio.file.AccessDeniedException

@Configuration
@EnableFeignClients(clients = [OpenLibraryClient::class, GoogleBooksClient::class])
class FeignClientConfig {

    @Bean
    fun errorDecoder(): ErrorDecoder = FeignErrorDecoder()


    @Bean
    fun retryer(): Retryer =
        Retryer.NEVER_RETRY
}

@Configuration
class FeignGoogleBooksConfig {
    @Bean
    fun feignAuthInterceptor(@Value($$"${app.service.google-books-api.auth.api-key}") apiKey: String): RequestInterceptor =
        RequestInterceptor { template ->
            template.query("key", apiKey)
        }
}

class FeignErrorDecoder : ErrorDecoder {

    private val log = logger()

    override fun decode(methodKey: String?, response: Response?): Exception? {
        //val status = HttpStatus.valueOf(response?.status() ?: HttpStatus.INTERNAL_SERVER_ERROR.value())
        val responseBody = extractResponseBody(response)

        log.error(
            "Feign client error. Method: {}, Status: {}, Body: {}",
            methodKey, response?.status(), responseBody
        )

        return when (response?.status()) {
            500, 502, 503, 504 ->
                RetryableException(
                    response.status(),
                    "Server error ${response.status()} from OpenLibrary",
                    response.request().httpMethod(),
                    1,
                    response.request()
                )

            429 ->
                RetryableException(
                    response.status(),
                    "Rate limit exceeded",
                    response.request().httpMethod(),
                    1,
                    response.request()
                )

            404 ->
                FeignException.NotFound(
                    "Not found",
                    response.request(),
                    null,
                    null
                )

            else ->
                FeignException.errorStatus(methodKey, response)
        }
    }

    private fun extractResponseBody(response: Response?): String {
        val body = response?.body() ?: return "No response body"

        return try {
            body.asInputStream().use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
        } catch (ex: IOException) {
            log.error("Failed to read response body", ex)
            "Error reading response body"
        }
    }
}