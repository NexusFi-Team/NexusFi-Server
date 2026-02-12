package com.nexusfi.server.infrastructure.security.jwt

import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.infrastructure.security.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

// JWT 토큰 생성 및 검증 담당 컴포넌트
@Component
@EnableConfigurationProperties(JwtProperties::class)
class JwtProvider(
    private val jwtProperties: JwtProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    // 보안 키 생성
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    companion object {
        private const val SOCIAL_TYPE_KEY = "social_type"
    }

    // Access Token 생성 (socialType 추가)
    fun createAccessToken(email: String, socialType: SocialType): String {
        return createToken(email, socialType, jwtProperties.accessTokenExpiration)
    }

    // Refresh Token 생성 (socialType 추가)
    fun createRefreshToken(email: String, socialType: SocialType): String {
        return createToken(email, socialType, jwtProperties.refreshTokenExpiration)
    }

    // 공통 토큰 생성 로직
    private fun createToken(email: String, socialType: SocialType, expiration: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(email)
            .claim(SOCIAL_TYPE_KEY, socialType.name) // socialType 클레임 추가
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    // 토큰에서 이메일 추출
    fun getEmail(token: String): String {
        return getClaims(token).subject
    }

    // 토큰에서 socialType 추출
    fun getSocialType(token: String): SocialType {
        val socialTypeName = getClaims(token).get(SOCIAL_TYPE_KEY, String::class.java)
        return SocialType.valueOf(socialTypeName)
    }

    // 토큰의 남은 유효 시간(ms) 계산
    fun getRemainingExpiration(token: String): Long {
        val expiration = getClaims(token).expiration
        return expiration.time - Date().time
    }

    // 토큰 유효성 검증 및 예외 발생
    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: SignatureException) {
            log.error("잘못된 JWT 서명임")
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        } catch (e: MalformedJwtException) {
            log.error("유효하지 않은 JWT 토큰임")
            throw BusinessException(ErrorCode.MALFORMED_TOKEN)
        } catch (e: ExpiredJwtException) {
            log.error("만료된 JWT 토큰임")
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        } catch (e: UnsupportedJwtException) {
            log.error("지원되지 않는 JWT 토큰임")
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        } catch (e: IllegalArgumentException) {
            log.error("JWT 토큰이 비어있음")
            throw BusinessException(ErrorCode.EMPTY_TOKEN)
        }
    }

    // 토큰의 Claims 추출
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
