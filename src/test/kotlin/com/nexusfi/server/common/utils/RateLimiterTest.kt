package com.nexusfi.server.common.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

class RateLimiterTest {

    private val redisTemplate = mockk<RedisTemplate<String, Any>>()
    private val valueOperations = mockk<ValueOperations<String, Any>>()
    private val rateLimiter = RateLimiter(redisTemplate)

    @Test
    @DisplayName("Rate Limit 허용 - 제한 횟수 이내인 경우 true를 반환한다")
    fun `isAllowed - success`() {
        // given
        val key = "test-key"
        val limit = 5
        val duration = 60L
        val redisKey = "rate_limit:$key"

        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.increment(redisKey) } returns 1L
        every { redisTemplate.expire(redisKey, duration, TimeUnit.SECONDS) } returns true

        // when
        val result = rateLimiter.isAllowed(key, limit, duration)

        // then
        assertTrue(result)
        verify { valueOperations.increment(redisKey) }
        verify { redisTemplate.expire(redisKey, duration, TimeUnit.SECONDS) }
    }

    @Test
    @DisplayName("Rate Limit 제한 - 제한 횟수를 초과한 경우 false를 반환한다")
    fun `isAllowed - fail`() {
        // given
        val key = "test-key"
        val limit = 5
        val duration = 60L
        val redisKey = "rate_limit:$key"

        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.increment(redisKey) } returns 6L

        // when
        val result = rateLimiter.isAllowed(key, limit, duration)

        // then
        assertFalse(result)
        verify { valueOperations.increment(redisKey) }
        verify(exactly = 0) { redisTemplate.expire(any(), any(), any()) }
    }
}
