package com.nexusfi.server.api.v1.auth

import com.nexusfi.server.api.v1.auth.dto.TokenResponse
import com.nexusfi.server.application.auth.AuthService
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.common.response.ApiResponse
import com.nexusfi.server.domain.user.model.UserId
import com.nexusfi.server.infrastructure.security.config.JwtProperties
import com.nexusfi.server.infrastructure.utils.CookieUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth API", description = "인증 관련 API (토큰 재발급, 로그아웃)")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieUtils: CookieUtils,
    private val jwtProperties: JwtProperties
) {

    @Operation(summary = "토큰 재발급", description = "쿠키의 Refresh Token을 사용하여 Access Token을 재발급합니다.")
    @PostMapping("/reissue")
    fun reissue(request: HttpServletRequest, response: HttpServletResponse): ApiResponse<TokenResponse> {
        // 1. 쿠키에서 Refresh Token 추출
        val refreshToken = cookieUtils.getCookieValue(request, "refreshToken")
            ?: throw BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        // 2. 서비스 로직 수행 (Rotation 적용)
        val (newAccessToken, newRefreshToken) = authService.reissue(refreshToken)

        // 3. 새 Refresh Token을 쿠키에 저장 (Http Only Cookie)
        val cookie = cookieUtils.createCookie(
            name = "refreshToken",
            value = newRefreshToken,
            maxAge = jwtProperties.refreshTokenExpiration / 1000
        )
        response.addHeader("Set-Cookie", cookie.toString())

        return ApiResponse.success(TokenResponse(newAccessToken))
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 수행하고 토큰을 무효화합니다.")
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userId: UserId,
        response: HttpServletResponse
    ): ApiResponse<Unit> {
        // 1. Redis에서 토큰 삭제
        authService.logout(userId.email)

        // 2. 쿠키 만료 처리
        val cookie = cookieUtils.deleteCookie("refreshToken")
        response.addHeader("Set-Cookie", cookie.toString())

        return ApiResponse.success(null, "로그아웃이 완료되었습니다.")
    }
}
