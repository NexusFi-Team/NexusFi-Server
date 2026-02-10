package com.nexusfi.server.infrastructure.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

// JWT 관련 설정을 담는 프로퍼티 클래스
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long
)
