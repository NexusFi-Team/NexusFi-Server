package com.nexusfi.server.infrastructure.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.common.response.ApiResponse
import com.nexusfi.server.infrastructure.security.handler.OAuth2SuccessHandler
import com.nexusfi.server.infrastructure.security.jwt.JwtFilter
import com.nexusfi.server.infrastructure.security.service.CustomOAuth2UserService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.cors.CorsUtils

// 스프링 시큐리티의 전역 설정을 담당하는 클래스
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties::class)
class SecurityConfig(
    private val securityProperties: SecurityProperties,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val jwtFilter: JwtFilter,
    private val objectMapper: ObjectMapper,
    private val securityContextRepository: SecurityContextRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화 (Stateless 환경이므로 필요 없음)
            .csrf { it.disable() }
            // 폼 로그인 및 기본 HTTP 로그인 비활성화
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            // 세션 정책을 Stateless로 설정 (JWT 사용)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            
            // SecurityContext 유지 설정 보강
            .securityContext { 
                it.securityContextRepository(securityContextRepository)
            }

            // 보안 예외 핸들링 (로그 및 응답 설정)
            .exceptionHandling { 
                it.authenticationEntryPoint(unauthorizedEntryPoint())
                it.accessDeniedHandler(accessDeniedHandler())
            }

            // 요청 권한 설정
            .authorizeHttpRequests { auth ->
                auth
                    // 화이트리스트 경로는 모두 허용
                    .requestMatchers(*securityProperties.whitelist.toTypedArray()).permitAll()
                    // Pre-flight 요청 허용
                    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                    // 그 외 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }

            // OAuth2 로그인 설정
            .oauth2Login { oauth ->
                oauth
                    .userInfoEndpoint { it.userService(customOAuth2UserService) }
                    .successHandler(oAuth2SuccessHandler)
            }

            // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    // 인증 실패 시 (401) 호출되는 엔트리 포인트
    private fun unauthorizedEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { request, response, authException ->
            log.warn("인증 실패 [{}]: {}", request.requestURI, authException?.message)
            
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED)
        }
    }

    // 인가 실패 시 (403) 호출되는 핸들러
    private fun accessDeniedHandler(): AccessDeniedHandler {
        return AccessDeniedHandler { request, response, accessDeniedException ->
            log.warn("인가 실패 [{}]: {}", request.requestURI, accessDeniedException?.message)
            
            sendErrorResponse(response, ErrorCode.HANDLE_ACCESS_DENIED)
        }
    }

    // 공통 에러 응답 전송 로직
    private fun sendErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        val apiResponse = ApiResponse.error(errorCode)
        val json = objectMapper.writeValueAsString(apiResponse)
        response.writer.write(json)
    }
}
