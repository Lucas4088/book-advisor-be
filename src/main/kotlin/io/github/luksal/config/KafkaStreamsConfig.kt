package io.github.luksal.config

import io.github.luksal.util.ext.logger
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.KafkaStreamsCustomizer


@Configuration
@EnableKafka
@EnableKafkaStreams
class KafkaStreamsConfig {

    companion object {
        val log = logger()
    }

    @Value($$"${spring.kafka.bootstrap-servers}")
    private val bootstrapAddress: String? = null

    @Bean
    fun kafkaStreamsCustomizer(): KafkaStreamsCustomizer {
        return KafkaStreamsCustomizer { kafkaStreams ->
            kafkaStreams.setUncaughtExceptionHandler { exception ->
                log.error("Uncaught exception in Kafka Streams", exception)
                StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION
            }
        }
    }

    @Bean
    fun defaultKafkaStreamsConfig(
        @Autowired props: KafkaProperties
    ): KafkaStreamsConfiguration {
        return KafkaStreamsConfiguration(
            mapOf(
                StreamsConfig.APPLICATION_ID_CONFIG to "streams-app",
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress as Any,
                )
        )
    }

}



