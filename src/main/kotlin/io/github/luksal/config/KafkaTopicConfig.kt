package io.github.luksal.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Value($$"${app.kafka.connect.books-source-topic}")
    private val sourceMongoTopic: String? = null

    @Value($$"${app.kafka.connect.books-sink-topic}")
    private val sinkPostgresTopic: String? = null

    @Bean
    fun sourceTopic(): NewTopic =
        TopicBuilder.name(sourceMongoTopic!!)
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun sinkTopic(): NewTopic =
        TopicBuilder.name(sinkPostgresTopic!!)
            .partitions(3)
            .replicas(1)
            .build()
}