package com.nexusfi.server.infrastructure.kafka

import com.nexusfi.server.common.dto.SecurityEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 카프카 토픽에서 보안 이벤트를 소비하여 실제 로그를 남기는 컨슈머
 */
@Component
class SecurityEventConsumer {
    
    // 보안 전용 로거 (logback-spring.xml 설정에 따름)
    private val securityLog = LoggerFactory.getLogger("SECURITY")
    
    companion object {
        private const val PREFIX = "[SECURITY_EVENT]"
    }

    /**
     * "security-events" 토픽을 실시간으로 구독하여 메시지 처리
     */
    @KafkaListener(
        topics = ["security-events"],
        groupId = "nexusfi-security-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeSecurityEvent(event: SecurityEvent) {
        val logMessage = "{} | Type: {} | User: {} | Message: {} | IP: {} | Timestamp: {}"
        val args = arrayOf(PREFIX, event.type, event.user, event.message, event.ip, event.timestamp)

        when (event.level.uppercase()) {
            "INFO" -> securityLog.info(logMessage, *args)
            "WARN" -> securityLog.warn(logMessage, *args)
            "ERROR" -> securityLog.error(logMessage, *args)
            else -> securityLog.info(logMessage, *args)
        }
    }
}
