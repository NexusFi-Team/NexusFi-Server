package com.nexusfi.server.common.utils

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 보안 감사 로그를 기록하는 전용 로거
 */
@Component
class SecurityLogger {
    /* "SECURITY" 전용 로거 생성 */
    private val log = LoggerFactory.getLogger("SECURITY")

    /**
     * 일반적인 보안 정보 기록 (로그인 성공 등)
     */
    fun info(type: String, user: String, info: String, ip: String? = null) {
        log.info("[INFO] [{}] [User: {}] [Msg: {}] [IP: {}]", type, user, info, formatIp(ip))
    }

    /**
     * 주의가 필요한 보안 이벤트 기록 (인증 실패, 블랙리스트 사용 등)
     */
    fun warn(type: String, user: String, reason: String, ip: String? = null) {
        log.warn("[WARN] [{}] [User: {}] [Msg: {}] [IP: {}]", type, user, reason, formatIp(ip))
    }

    /**
     * 심각한 보안 위협 기록 (토큰 탈취 의심 등)
     */
    fun error(type: String, user: String, message: String, ip: String? = null) {
        log.error("[ERROR] [{}] [User: {}] [Msg: {}] [IP: {}]", type, user, message, formatIp(ip))
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
