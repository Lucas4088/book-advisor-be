package io.github.luksal.config

import io.github.luksal.book.db.document.book.BookDocument
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import tools.jackson.databind.deser.jdk.StringDeserializer

@Configuration
class KafkaConfig {

    @Bean
    fun adminClient(kafkaProperties: KafkaProperties): AdminClient =
        AdminClient.create(kafkaProperties.buildAdminProperties())

    @Bean
    fun bookConsumerFactory(props: KafkaProperties): ConsumerFactory<String, BookDocument> {
        val deserializer = JacksonJsonDeserializer(BookDocument::class.java)
        val consumerProps = props.buildConsumerProperties()

        consumerProps[ConsumerConfig.GROUP_ID_CONFIG] = "book-advisor-consumer"
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java

        consumerProps[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
        consumerProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JacksonJsonDeserializer::class.java

        consumerProps[JacksonJsonDeserializer.TRUSTED_PACKAGES] = "*"
        consumerProps[JacksonJsonDeserializer.VALUE_DEFAULT_TYPE] = BookDocument::class.java.name
        return DefaultKafkaConsumerFactory<String, BookDocument>(
            consumerProps,
            org.apache.kafka.common.serialization.StringDeserializer(),
            deserializer
        )
    }

    @Bean
    fun bookKafkaListenerContainerFactory(
        bookConsumerFactory: ConsumerFactory<String, BookDocument>
    ): ConcurrentKafkaListenerContainerFactory<String, BookDocument> {

        val factory = ConcurrentKafkaListenerContainerFactory<String, BookDocument>()
        factory.setConsumerFactory(bookConsumerFactory)
        return factory
    }
}