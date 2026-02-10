package com.nexusfi.server.infrastructure.security.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

// 인증 완료 후 SecurityContext에 저장할 커스텀 사용자 객체
class CustomOAuth2User(
    private val email: String,
    private val attributes: Map<String, Any>
) : OAuth2User {

    // 로그인 사용자에게 기본 ROLE_USER 권한 부여
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    // 소셜 원본 속성 반환
    override fun getAttributes(): MutableMap<String, Any> {
        return attributes.toMutableMap()
    }

    // 시큐리티 식별자로 이메일 사용
    override fun getName(): String {
        return email
    }

    // 이메일 획득
    fun getEmail(): String {
        return email
    }
}