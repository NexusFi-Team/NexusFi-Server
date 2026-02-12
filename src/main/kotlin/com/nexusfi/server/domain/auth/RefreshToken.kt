package com.nexusfi.server.domain.auth

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

// Redis에 저장될 리프레시 토큰 정보
@RedisHash(value = "refresh_token")
class RefreshToken(
    @Id
    val email: String,
    
    val token: String,
    
    // 만료 시간 (초 단위)
    @TimeToLive
    val expiration: Long
)
