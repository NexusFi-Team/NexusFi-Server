package com.nexusfi.server.common.dto

import java.time.LocalDateTime

/**
 * 카프카를 통해 스트리밍될 보안 이벤트 데이터 구조
 */
data class SecurityEvent(
    val type: String,
    val user: String,
    val message: String,
    val ip: String,
    val level: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
