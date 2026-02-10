package com.nexusfi.server.infrastructure.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

// 보안 관련 설정을 담는 프로퍼티 클래스
@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    val whitelist: List<String>
)
