package com.nexusfi.server.infrastructure.utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

// 쿠키 관련 조작을 담당하는 유틸리티 컴포넌트
@Component
class CookieUtils {

    // 새로운 쿠키 생성 (설정 공통화)
    fun createCookie(name: String, value: String, maxAge: Long): ResponseCookie {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(false) // 로컬 테스트 편의를 위해 false 고정 (추후 설정 분리 가능)
            .path("/")
            .maxAge(maxAge)
            .sameSite("Lax")
            .build()
    }

    // 요청에서 특정 이름의 쿠키 값 추출
    fun getCookieValue(request: HttpServletRequest, name: String): String? {
        return request.cookies?.find { it.name == name }?.value
    }

    // 쿠키 삭제를 위한 만료 쿠키 생성
    fun deleteCookie(name: String): ResponseCookie {
        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0) // 즉시 만료
            .sameSite("Lax")
            .build()
    }
}
