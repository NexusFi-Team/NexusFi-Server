package com.nexusfi.server.infrastructure.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.common.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// 매 요청마다 JWT 토큰 유효성을 검사하는 필터
@Component
class JwtFilter(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        try {
            if (token != null && jwtProvider.validateToken(token)) {
                val email = jwtProvider.getEmail(token)
                
                // 인증 객체 생성 및 SecurityContext 저장
                val authentication = UsernamePasswordAuthenticationToken(
                    email, null, listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
            filterChain.doFilter(request, response)
        } catch (e: BusinessException) {
            // 필터 체인 내에서 발생한 토큰 예외 처리
            sendErrorResponse(response, e.errorCode)
        } catch (e: Exception) {
            // 기타 예상치 못한 예외 처리
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    // Authorization 헤더에서 토큰 추출
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    // 예외 발생 시 ApiResponse 규격으로 JSON 응답 전송
    private fun sendErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val apiResponse = ApiResponse.error(errorCode)
        val json = objectMapper.writeValueAsString(apiResponse)
        response.writer.write(json)
    }
}
