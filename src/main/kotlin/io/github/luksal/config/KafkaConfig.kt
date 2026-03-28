package io.github.luksal.config

import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.document.book.RatingDocument
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import tools.jackson.databind.deser.jdk.StringDeserializer


@Configuration
class KafkaConfig(

) {

    @Bean
    fun adminClient(kafkaProperties: KafkaProperties): AdminClient =
        AdminClient.create(kafkaProperties.buildAdminProperties())

    @Bean
    fun bookConsumerFactory(props: KafkaProperties): ConsumerFactory<String, BookDocument> =
        createConsumerFactory(props, "book-consumer-group", BookDocument::class.java)

    @Bean
    fun ratingConsumerFactory(props: KafkaProperties): ConsumerFactory<String, RatingDocument> =
        createConsumerFactory(props, "rating-consumer-group", RatingDocument::class.java)

    @Bean
    fun bookKafkaListenerContainerFactory(
        bookConsumerFactory: ConsumerFactory<String, BookDocument>,
        dlqKafkaTemplate: KafkaTemplate<String, Any>,
        mongoBooksDlqTopic: NewTopic,
    ): ConcurrentKafkaListenerContainerFactory<String, BookDocument> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, BookDocument>()
        factory.setConsumerFactory(bookConsumerFactory)
        factory.setCommonErrorHandler(errorHandler(dlqKafkaTemplate, mongoBooksDlqTopic))
        return factory
    }

    @Bean
    fun dlqKafkaTemplate(kafkaProperties: KafkaProperties): KafkaTemplate<String, Any> {
        val props = kafkaProperties.buildProducerProperties()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = "org.springframework.kafka.support.serializer.JacksonJsonSerializer"
        val factory = DefaultKafkaProducerFactory<String, Any>(props)
        return KafkaTemplate(factory)
    }

    fun errorHandler(kafkaTemplate: KafkaTemplate<String, Any>, mongoBooksDlqTopic: NewTopic): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(
            kafkaTemplate
        ) { record: ConsumerRecord<*, *>?, ex: Exception? ->
            TopicPartition(
                mongoBooksDlqTopic.name(),
                record!!.partition()
            )
        }
        recoverer.setFailIfSendResultIsError(false)

        val backOff = ExponentialBackOffWithMaxRetries(3)
        backOff.initialInterval = 1000L
        backOff.multiplier = 2.0

        return DefaultErrorHandler(recoverer, backOff)
    }

    @Bean
    fun ratingKafkaListenerContainerFactory(
        ratingConsumerFactory: ConsumerFactory<String, RatingDocument>
    ): ConcurrentKafkaListenerContainerFactory<String, RatingDocument> {

        val factory = ConcurrentKafkaListenerContainerFactory<String, RatingDocument>()
        factory.setConsumerFactory(ratingConsumerFactory)
        return factory
    }

    private fun <T : Any> createConsumerFactory(
        props: KafkaProperties,
        groupId: String,
        valueClass: Class<T>
    ): ConsumerFactory<String, T> {

        val deserializer = JacksonJsonDeserializer(valueClass)
        val consumerProps = props.buildConsumerProperties()

        consumerProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java

        consumerProps[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
        consumerProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JacksonJsonDeserializer::class.java

        consumerProps[JacksonJsonDeserializer.TRUSTED_PACKAGES] = "*"
        consumerProps[JacksonJsonDeserializer.VALUE_DEFAULT_TYPE] = valueClass.name

        return DefaultKafkaConsumerFactory(
            consumerProps,
            org.apache.kafka.common.serialization.StringDeserializer(),
            deserializer
        )
    }
}