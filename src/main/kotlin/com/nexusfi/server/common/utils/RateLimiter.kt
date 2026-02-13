package com.nexusfi.server.common.utils

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

// Redis를 활용하여 특정 요청의 빈도를 제한하는 컴포넌트
@Component
class RateLimiter(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private const val PREFIX = "rate_limit:"
    }

    /**
     * 요청이 제한 범위 내에 있는지 확인
     * @param key 식별 키 (예: reissue:user@email.com)
     * @param limit 제한 횟수
     * @param durationSeconds 제한 시간 (초)
     * @return 허용 여부 (true: 허용, false: 제한 초과)
     */
    fun isAllowed(key: String, limit: Int, durationSeconds: Long): Boolean {
        val redisKey = PREFIX + key
        
        // Redis의 increment 연산은 원자적(Atomic)으로 동작함
        val currentCount = redisTemplate.opsForValue().increment(redisKey) ?: 0L

        // 첫 요청인 경우 만료 시간 설정
        if (currentCount == 1L) {
            redisTemplate.expire(redisKey, durationSeconds, TimeUnit.SECONDS)
        }

        return currentCount <= limit
    }
}
