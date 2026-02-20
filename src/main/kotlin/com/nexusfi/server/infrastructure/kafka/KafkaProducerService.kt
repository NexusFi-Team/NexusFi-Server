package com.nexusfi.server.infrastructure.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * 카프카 토픽에 메시지를 발행하는 프로듀서 서비스
 */
@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 특정 토픽으로 메시지 전송
     * @param topic 메시지가 저장될 토픽 이름
     * @param message 전송할 데이터
     */
    fun sendMessage(topic: String, message: Any) {
        log.info("Sending message to Kafka [Topic: {}]: {}", topic, message)
        
        // 비동기로 메시지 전송 및 결과 처리
        kafkaTemplate.send(topic, message)
            .whenComplete { result, ex ->
                if (ex == null) {
                    log.info("Message sent successfully: {}", result.recordMetadata.offset())
                } else {
                    log.error("Failed to send message", ex)
                }
            }
    }
}
