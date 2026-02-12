package com.nexusfi.server.application.auth

import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.auth.RefreshToken
import com.nexusfi.server.domain.auth.repository.RefreshTokenRepository
import com.nexusfi.server.infrastructure.security.config.JwtProperties
import com.nexusfi.server.infrastructure.security.jwt.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 인증 관련 비즈니스 로직을 처리하는 서비스
@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties
) {

    // 토큰 재발급 (Refresh Token Rotation 적용)
    @Transactional
    fun reissue(refreshToken: String): Pair<String, String> {
        // 1. 토큰 유효성 검증
        jwtProvider.validateToken(refreshToken)

        // 2. 토큰에서 정보 추출
        val email = jwtProvider.getEmail(refreshToken)
        val socialType = jwtProvider.getSocialType(refreshToken)

        // 3. Redis에서 기존 토큰 확인 및 검증
        val savedToken = refreshTokenRepository.findById(email)
            .orElseThrow { BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND) }

        if (savedToken.token != refreshToken) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        // 4. 새로운 토큰 쌍 생성
        val newAccessToken = jwtProvider.createAccessToken(email, socialType)
        val newRefreshToken = jwtProvider.createRefreshToken(email, socialType)

        // 5. Redis 정보 갱신 (Rotation)
        refreshTokenRepository.delete(savedToken)
        refreshTokenRepository.save(
            RefreshToken(
                email = email,
                token = newRefreshToken,
                expiration = jwtProperties.refreshTokenExpiration / 1000
            )
        )

        return Pair(newAccessToken, newRefreshToken)
    }

    // 로그아웃 (Redis 데이터 삭제)
    @Transactional
    fun logout(email: String) {
        val savedToken = refreshTokenRepository.findById(email)
            .orElse(null)
        
        savedToken?.let {
            refreshTokenRepository.delete(it)
        }
    }
}
