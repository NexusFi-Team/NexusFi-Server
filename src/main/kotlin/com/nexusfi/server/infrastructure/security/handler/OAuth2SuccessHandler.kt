package com.nexusfi.server.infrastructure.security.handler

import com.nexusfi.server.infrastructure.security.dto.CustomOAuth2User
import com.nexusfi.server.infrastructure.security.jwt.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

// 소셜 로그인 성공 시 실행되는 핸들러
@Component
class OAuth2SuccessHandler(
    private val jwtProvider: JwtProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as CustomOAuth2User
        val email = oAuth2User.getEmail()

        // Access Token 생성
        val accessToken = jwtProvider.createAccessToken(email)

        // 프론트엔드로 리다이렉트할 URL 생성 (토큰 포함)
        val targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login/callback")
            .queryParam("token", accessToken)
            .build().toUriString()

        // 리다이렉트 수행
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
