package io.github.luksal.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.json.JsonMapper
import java.time.Duration
import java.util.concurrent.TimeUnit


@EnableCaching
@Configuration
class CacheConfig {

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> =
        Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES)

    @Bean
    @Primary
    fun caffeineCacheManager(caffeineConfig: Caffeine<Any, Any>): CacheManager =
        CaffeineCacheManager().apply {
            setCaffeine(caffeineConfig)
        }

    @Bean
    fun redisSerializer(): GenericJacksonJsonRedisSerializer {
        val mapper = JsonMapper.builder()
            .findAndAddModules() // JavaTimeModule, KotlinModule, etc.
            .build()

        return GenericJacksonJsonRedisSerializer(mapper)
    }

    @Bean
    fun redisTemplate(redisSerializer: GenericJacksonJsonRedisSerializer, redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<Any?, Any?> =
      RedisTemplate<Any?, Any?>().apply {
          connectionFactory = redisConnectionFactory
          keySerializer = StringRedisSerializer()
          hashKeySerializer = StringRedisSerializer()
          valueSerializer = redisSerializer
          hashValueSerializer = redisSerializer
      }


    @Bean
    fun redisCacheManager(redisSerializer: GenericJacksonJsonRedisSerializer, redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val config = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
            )
            .entryTtl(Duration.ofDays(30))
        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build()
    }
}