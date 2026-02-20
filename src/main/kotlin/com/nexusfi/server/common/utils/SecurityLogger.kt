package com.nexusfi.server.common.utils

import com.nexusfi.server.common.dto.SecurityEvent
import com.nexusfi.server.infrastructure.kafka.KafkaProducerService
import org.springframework.stereotype.Component

/**
 * 보안 이벤트를 카프카로 발행하는 프로듀서
 */
@Component
class SecurityLogger(
    private val kafkaProducerService: KafkaProducerService
) {
    companion object {
        private const val TOPIC = "security-events"
    }

    /**
     * 일반적인 보안 정보 기록 (로그인 성공 등)
     */
    fun info(type: String, user: String, info: String, ip: String? = null) {
        val event = SecurityEvent(
            type = type,
            user = user,
            message = info,
            ip = formatIp(ip),
            level = "INFO"
        )
        kafkaProducerService.sendMessage(TOPIC, event)
    }

    /**
     * 주의가 필요한 보안 이벤트 기록 (인증 실패, 블랙리스트 사용 등)
     */
    fun warn(type: String, user: String, reason: String, ip: String? = null) {
        val event = SecurityEvent(
            type = type,
            user = user,
            message = reason,
            ip = formatIp(ip),
            level = "WARN"
        )
        kafkaProducerService.sendMessage(TOPIC, event)
    }

    /**
     * 심각한 보안 위협 기록 (토큰 탈취 의심 등)
     */
    fun error(type: String, user: String, message: String, ip: String? = null) {
        val event = SecurityEvent(
            type = type,
            user = user,
            message = message,
            ip = formatIp(ip),
            level = "ERROR"
        )
        kafkaProducerService.sendMessage(TOPIC, event)
    }

    /**
     * IPv6 루프백 주소를 IPv4로 변환
     */
    private fun formatIp(ip: String?): String {
        return when (ip) {
            null -> "unknown"
            "0:0:0:0:0:0:0:1" -> "127.0.0.1"
            else -> ip
        }
    }
}
