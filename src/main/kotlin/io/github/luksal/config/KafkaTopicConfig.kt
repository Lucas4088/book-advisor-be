package io.github.luksal.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Value($$"${app.kafka.connect.books-source-topic}")
    private val sourceMongoTopic: String? = null


    @Value($$"${app.kafka.consumer.books-source-dlq-topic}")
    private val mongoBooksDlqTopic: String? = null

    @Bean
    fun sourceTopic(): NewTopic =
        TopicBuilder.name(sourceMongoTopic!!)
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun mongoBooksDlqTopic(): NewTopic =
        TopicBuilder.name(mongoBooksDlqTopic!!)
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun connectOffsetsTopic(): NewTopic =
        TopicBuilder.name("connect-offsets")
            .partitions(25)
            .replicas(1)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
            .build()

    @Bean
    fun connectConfigsTopic(): NewTopic =
        TopicBuilder.name("connect-configs")
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
            .build()

    @Bean
    fun connectStatusTopic(): NewTopic =
        TopicBuilder.name("connect-status")
            .partitions(5)
            .replicas(1)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
            .build()
}