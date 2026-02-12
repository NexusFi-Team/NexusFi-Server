package com.nexusfi.server.infrastructure.security.handler

import com.nexusfi.server.domain.auth.RefreshToken
import com.nexusfi.server.domain.auth.repository.RefreshTokenRepository
import com.nexusfi.server.infrastructure.security.config.JwtProperties
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
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as CustomOAuth2User
        val email = oAuth2User.getEmail()
        val socialType = oAuth2User.getSocialType()

        // 두 종류의 토큰 생성 (socialType 포함)
        val accessToken = jwtProvider.createAccessToken(email, socialType)
        val refreshToken = jwtProvider.createRefreshToken(email, socialType)

        // 리프레시 토큰을 Redis에 저장 (만료 시간은 초 단위로 변환)
        refreshTokenRepository.save(
            RefreshToken(
                email = email,
                token = refreshToken,
                expiration = jwtProperties.refreshTokenExpiration / 1000
            )
        )

        // 프론트엔드로 리다이렉트할 URL 생성 (두 토큰 모두 포함)
        val targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login/callback")
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString()

        // 리다이렉트 수행
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
