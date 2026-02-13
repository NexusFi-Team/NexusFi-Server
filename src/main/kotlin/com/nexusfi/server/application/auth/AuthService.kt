package com.nexusfi.server.application.auth

import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.common.utils.RateLimiter
import com.nexusfi.server.common.utils.SecurityAudit
import com.nexusfi.server.domain.auth.RefreshToken
import com.nexusfi.server.domain.auth.repository.RefreshTokenRepository
import com.nexusfi.server.infrastructure.security.config.JwtProperties
import com.nexusfi.server.infrastructure.security.jwt.JwtProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

// 인증 관련 비즈니스 로직을 처리하는 서비스 (Coroutine 적용)
@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val rateLimiter: RateLimiter
) {

    companion object {
        private const val BLACKLIST_PREFIX = "black_list:"
    }

    // 토큰 재발급 (Refresh Token Rotation 적용)
    @SecurityAudit("TOKEN_REISSUE")
    @Transactional
    suspend fun reissue(refreshToken: String): Pair<String, String> = coroutineScope {
        // 1. 토큰 유효성 검증
        jwtProvider.validateToken(refreshToken)

        // 2. 토큰에서 정보 추출
        val email = jwtProvider.getEmail(refreshToken)
        val socialType = jwtProvider.getSocialType(refreshToken)

        // 3. 요청 빈도 제한 확인 (1분당 5회)
        if (!rateLimiter.isAllowed("reissue:$email", 5, 60)) {
            throw BusinessException(ErrorCode.TOO_MANY_REQUESTS)
        }

        // 4. Redis에서 기존 토큰 확인 및 검증
        val savedToken = refreshTokenRepository.findById(email)
            .orElseThrow { BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND) }

        if (savedToken.token != refreshToken) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        // 5. 새로운 토큰 쌍 생성
        val newAccessToken = jwtProvider.createAccessToken(email, socialType)
        val newRefreshToken = jwtProvider.createRefreshToken(email, socialType)

        // 6. Redis 정보 갱신
        refreshTokenRepository.delete(savedToken)
        refreshTokenRepository.save(
            RefreshToken(
                email = email,
                token = newRefreshToken,
                expiration = jwtProperties.refreshTokenExpiration / 1000
            )
        )

        Pair(newAccessToken, newRefreshToken)
    }

    // 로그아웃 (Redis 데이터 삭제 및 블랙리스트 등록)
    @SecurityAudit("LOGOUT")
    @Transactional
    suspend fun logout(email: String, accessToken: String) = coroutineScope {
        // 비동기 병렬 처리: 리프레시 토큰 삭제와 블랙리스트 등록 동시 진행
        val deleteJob = async {
            val savedToken = refreshTokenRepository.findById(email).orElse(null)
            savedToken?.let { refreshTokenRepository.delete(it) }
        }

        val blacklistJob = async {
            val remainingTime = jwtProvider.getRemainingExpiration(accessToken)
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "logout",
                    remainingTime,
                    TimeUnit.MILLISECONDS
                )
            }
        }

        deleteJob.await()
        blacklistJob.await()
    }

    // 토큰의 블랙리스트 여부 확인
    fun isBlacklisted(accessToken: String): Boolean {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken) ?: false
    }
}
