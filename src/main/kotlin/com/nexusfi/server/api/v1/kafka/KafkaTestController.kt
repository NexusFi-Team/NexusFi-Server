package com.nexusfi.server.api.v1.kafka

import com.nexusfi.server.common.response.ApiResponse
import com.nexusfi.server.infrastructure.kafka.KafkaProducerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Kafka Test API", description = "카프카 메시지 송수신 테스트 API")
@RestController
@RequestMapping("/api/v1/kafka")
class KafkaTestController(
    private val kafkaProducerService: KafkaProducerService
) {

    @Operation(summary = "메시지 전송 테스트", description = "특정 토픽으로 테스트 메시지를 전송합니다.")
    @GetMapping("/test")
    fun sendTestMessage(
        @RequestParam(defaultValue = "test-topic") topic: String,
        @RequestParam(defaultValue = "Hello Kafka!") message: String
    ): ApiResponse<String> {
        kafkaProducerService.sendMessage(topic, message)
        
        return ApiResponse.success("Message sent to Kafka topic: $topic")
    }
}
