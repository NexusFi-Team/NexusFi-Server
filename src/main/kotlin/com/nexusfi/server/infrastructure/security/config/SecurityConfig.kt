package com.nexusfi.server.infrastructure.security.config

import com.nexusfi.server.infrastructure.security.handler.OAuth2SuccessHandler
import com.nexusfi.server.infrastructure.security.jwt.JwtFilter
import com.nexusfi.server.infrastructure.security.service.CustomOAuth2UserService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsUtils

// 스프링 시큐리티의 전역 설정을 담당하는 클래스
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties::class)
class SecurityConfig(
    private val securityProperties: SecurityProperties,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val jwtFilter: JwtFilter
) {

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
}
