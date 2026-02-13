package com.nexusfi.server.infrastructure.security.aop

import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.common.utils.SecurityAudit
import com.nexusfi.server.common.utils.SecurityLogger
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.UserId
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class SecurityAuditAspectTest {

    private val securityLogger = mockk<SecurityLogger>(relaxed = true)
    private val aspect = SecurityAuditAspect(securityLogger)
    private val joinPoint = mockk<ProceedingJoinPoint>()
    private val securityAudit = mockk<SecurityAudit>()
    
    private val testEmail = "test@example.com"
    private val testIp = "127.0.0.1"
    private val auditType = "TEST_EVENT"

    @BeforeEach
    fun setUp() {
        // 어노테이션 설정
        every { securityAudit.type } returns auditType

        // 정적 모킹 시작
        mockkStatic(RequestContextHolder::class)
        mockkStatic(SecurityContextHolder::class)

        // IP 모킹
        val request = mockk<HttpServletRequest>()
        val attributes = mockk<ServletRequestAttributes>()
        every { RequestContextHolder.getRequestAttributes() } returns attributes
        every { attributes.request } returns request
        every { request.remoteAddr } returns testIp

        // SecurityContext 모킹
        val securityContext = mockk<SecurityContext>()
        val authentication = mockk<Authentication>()
        val userId = UserId(testEmail, SocialType.GOOGLE)
        
        every { SecurityContextHolder.getContext() } returns securityContext
        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns userId
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(RequestContextHolder::class)
        unmockkStatic(SecurityContextHolder::class)
        clearAllMocks()
    }

    @Test
    @DisplayName("AOP 성공 테스트 - 메서드가 정상 실행되면 info 로그를 남긴다")
    fun `audit success`() {
        // given
        every { joinPoint.proceed() } returns "Success Result"

        // when
        val result = aspect.audit(joinPoint, securityAudit)

        // then
        assertEquals("Success Result", result)
        verify { securityLogger.info(auditType, testEmail, "Success", testIp) }
    }

    @Test
    @DisplayName("AOP 비즈니스 예외 테스트 - BusinessException 발생 시 warn 로그를 남긴다")
    fun `audit business fail`() {
        // given
        val exception = BusinessException(ErrorCode.INVALID_TOKEN)
        every { joinPoint.proceed() } throws exception

        // when & then
        assertThrows(BusinessException::class.java) {
            aspect.audit(joinPoint, securityAudit)
        }
        verify { securityLogger.warn(auditType, testEmail, "Fail: ${ErrorCode.INVALID_TOKEN.message}", testIp) }
    }

    @Test
    @DisplayName("AOP 일반 예외 테스트 - Exception 발생 시 error 로그를 남긴다")
    fun `audit unexpected fail`() {
        // given
        val exception = RuntimeException("Unexpected error")
        every { joinPoint.proceed() } throws exception

        // when & then
        assertThrows(RuntimeException::class.java) {
            aspect.audit(joinPoint, securityAudit)
        }
        verify { securityLogger.error(auditType, testEmail, "Error: Unexpected error", testIp) }
    }
}
