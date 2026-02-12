package com.nexusfi.server.common.utils

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.Logger

class SecurityLoggerTest {

    private val mockLogger = mockk<Logger>(relaxed = true)
    private val securityLogger = SecurityLogger(mockLogger)

    @Test
    @DisplayName("보안 정보 로그가 올바른 규격으로 생성되는지 확인한다")
    fun `info log format test`() {
        // given
        val type = "LOGIN_SUCCESS"
        val user = "test@example.com"
        val info = "Provider: GOOGLE"
        val ip = "127.0.0.1"

        // when
        securityLogger.info(type, user, info, ip)

        // then
        verify {
            mockLogger.info(
                "{} | Type: {} | User: {} | Info: {} | IP: {}",
                "[SECURITY_EVENT]", type, user, info, ip
            )
        }
    }

    @Test
    @DisplayName("보안 경고 로그가 올바른 규격으로 생성되는지 확인한다")
    fun `warn log format test`() {
        // given
        val type = "AUTH_FAILURE"
        val user = "unknown"
        val reason = "Invalid Token"

        // when
        securityLogger.warn(type, user, reason)

        // then
        verify {
            mockLogger.warn(
                "{} | Type: {} | User: {} | Reason: {} | IP: {}",
                "[SECURITY_EVENT]", type, user, reason, "unknown"
            )
        }
    }

    @Test
    @DisplayName("보안 에러 로그가 올바른 규격으로 생성되는지 확인한다")
    fun `error log format test`() {
        // given
        val type = "TOKEN_THEFT"
        val user = "hacker@example.com"
        val message = "Token mismatch detected"

        // when
        securityLogger.error(type, user, message)

        // then
        verify {
            mockLogger.error(
                "{} | Type: {} | User: {} | Message: {} | IP: {}",
                "[SECURITY_EVENT]", type, user, message, "unknown"
            )
        }
    }
}
