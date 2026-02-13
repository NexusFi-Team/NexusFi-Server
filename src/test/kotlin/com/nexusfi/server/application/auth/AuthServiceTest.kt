package com.nexusfi.server.application.auth

import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.auth.RefreshToken
import com.nexusfi.server.domain.auth.repository.RefreshTokenRepository
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.infrastructure.security.config.JwtProperties
import com.nexusfi.server.infrastructure.security.jwt.JwtProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.*
import java.util.concurrent.TimeUnit

class AuthServiceTest {

    private val jwtProvider = mockk<JwtProvider>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtProperties = mockk<JwtProperties>()
    private val redisTemplate = mockk<RedisTemplate<String, Any>>()
    private val valueOperations = mockk<ValueOperations<String, Any>>()
    private val securityLogger = mockk<com.nexusfi.server.common.utils.SecurityLogger>(relaxed = true)
    private val rateLimiter = mockk<com.nexusfi.server.common.utils.RateLimiter>()

    private val authService = AuthService(
        jwtProvider,
        refreshTokenRepository,
        jwtProperties,
        redisTemplate,
        securityLogger,
        rateLimiter
    )

    private val email = "test@example.com"
    private val socialType = SocialType.GOOGLE
    private val refreshToken = "old-refresh-token"
    private val accessToken = "old-access-token"

    @Test
    @DisplayName("토큰 재발급 성공 - 새로운 토큰 쌍을 반환하고 Redis를 갱신한다")
    fun `reissue success`() = runTest {
        // given
        val newAccessToken = "new-access-token"
        val newRefreshToken = "new-refresh-token"
        val savedToken = RefreshToken(email, refreshToken, 1209600)

        every { jwtProvider.validateToken(refreshToken) } returns true
        every { jwtProvider.getEmail(refreshToken) } returns email
        every { jwtProvider.getSocialType(refreshToken) } returns socialType
        every { rateLimiter.isAllowed("reissue:$email", 5, 60) } returns true
        every { refreshTokenRepository.findById(email) } returns Optional.of(savedToken)
        every { jwtProvider.createAccessToken(email, socialType) } returns newAccessToken
        every { jwtProvider.createRefreshToken(email, socialType) } returns newRefreshToken
        every { jwtProperties.refreshTokenExpiration } returns 1209600000L
        every { refreshTokenRepository.delete(savedToken) } returns Unit
        every { refreshTokenRepository.save(any()) } returns savedToken

        // when
        val result = authService.reissue(refreshToken)

        // then
        assertEquals(newAccessToken, result.first)
        assertEquals(newRefreshToken, result.second)
        verify { refreshTokenRepository.delete(savedToken) }
        verify { refreshTokenRepository.save(match { it.email == email && it.token == newRefreshToken }) }
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 요청 빈도 제한을 초과한 경우 예외가 발생한다")
    fun `reissue fail - rate limit exceeded`() = runTest {
        // given
        every { jwtProvider.validateToken(refreshToken) } returns true
        every { jwtProvider.getEmail(refreshToken) } returns email
        every { jwtProvider.getSocialType(refreshToken) } returns socialType
        every { rateLimiter.isAllowed("reissue:$email", 5, 60) } returns false

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            kotlinx.coroutines.runBlocking { authService.reissue(refreshToken) }
        }
        assertEquals(ErrorCode.TOO_MANY_REQUESTS, exception.errorCode)
        verify { securityLogger.warn("RATE_LIMIT_EXCEEDED", email, any()) }
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis에 토큰이 없는 경우 예외가 발생한다")
    fun `reissue fail - token not found`() = runTest {
        // given
        every { jwtProvider.validateToken(refreshToken) } returns true
        every { jwtProvider.getEmail(refreshToken) } returns email
        every { jwtProvider.getSocialType(refreshToken) } returns socialType
        every { rateLimiter.isAllowed("reissue:$email", 5, 60) } returns true
        every { refreshTokenRepository.findById(email) } returns Optional.empty()

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            kotlinx.coroutines.runBlocking { authService.reissue(refreshToken) }
        }
        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.errorCode)
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 요청 토큰과 Redis 저장 토큰이 다른 경우 예외가 발생한다")
    fun `reissue fail - token mismatch`() = runTest {
        // given
        val savedToken = RefreshToken(email, "different-token", 1209600)

        every { jwtProvider.validateToken(refreshToken) } returns true
        every { jwtProvider.getEmail(refreshToken) } returns email
        every { jwtProvider.getSocialType(refreshToken) } returns socialType
        every { rateLimiter.isAllowed("reissue:$email", 5, 60) } returns true
        every { refreshTokenRepository.findById(email) } returns Optional.of(savedToken)

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            kotlinx.coroutines.runBlocking { authService.reissue(refreshToken) }
        }
        assertEquals(ErrorCode.INVALID_TOKEN, exception.errorCode)
    }

    @Test
    @DisplayName("로그아웃 성공 - Redis에서 토큰을 삭제하고 블랙리스트에 등록한다")
    fun `logout success`() = runTest {
        // given
        val savedToken = RefreshToken(email, refreshToken, 1209600)
        val remainingTime = 3600000L // 1 hour

        every { refreshTokenRepository.findById(email) } returns Optional.of(savedToken)
        every { refreshTokenRepository.delete(savedToken) } returns Unit
        every { jwtProvider.getRemainingExpiration(accessToken) } returns remainingTime
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.set("black_list:$accessToken", "logout", remainingTime, TimeUnit.MILLISECONDS) } returns Unit

        // when
        authService.logout(email, accessToken)

        // then
        verify { refreshTokenRepository.delete(savedToken) }
        verify { valueOperations.set("black_list:$accessToken", "logout", remainingTime, TimeUnit.MILLISECONDS) }
    }
}
