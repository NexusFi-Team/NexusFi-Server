package com.nexusfi.server.common.utils

/**
 * 보안 감사 로그를 남기기 위한 커스텀 어노테이션
 * @property type 보안 이벤트 타입 (예: LOGIN, LOGOUT, REISSUE, WITHDRAWAL 등)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SecurityAudit(
    val type: String
)
