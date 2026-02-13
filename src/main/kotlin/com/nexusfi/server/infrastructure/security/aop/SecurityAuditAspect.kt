package com.nexusfi.server.infrastructure.security.aop

import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.utils.SecurityAudit
import com.nexusfi.server.common.utils.SecurityLogger
import com.nexusfi.server.domain.user.model.UserId
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class SecurityAuditAspect(
    private val securityLogger: SecurityLogger
) {

    @Around("@annotation(securityAudit)")
    fun audit(joinPoint: ProceedingJoinPoint, securityAudit: SecurityAudit): Any? {
        val type = securityAudit.type
        val userEmail = getCurrentUserEmail()
        val ip = getClientIp()

        return try {
            val result = joinPoint.proceed()
            // 메서드 실행 성공 시 로그 기록
            securityLogger.info(type, userEmail, "Success", ip)
            result
        } catch (e: BusinessException) {
            // 비즈니스 예외 발생 시 (토큰 만료 등)
            securityLogger.warn(type, userEmail, "Fail: ${e.errorCode.message}", ip)
            throw e
        } catch (e: Exception) {
            // 알 수 없는 예외 발생 시
            securityLogger.error(type, userEmail, "Error: ${e.message}", ip)
            throw e
        }
    }

    // 현재 요청의 클라이언트 IP 추출
    private fun getClientIp(): String {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        val request = attributes?.request
        return request?.remoteAddr ?: "unknown"
    }

    // 현재 인증된 유저의 이메일 추출
    private fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.principal is UserId) {
            return (authentication.principal as UserId).email
        }
        return "unknown"
    }
}
