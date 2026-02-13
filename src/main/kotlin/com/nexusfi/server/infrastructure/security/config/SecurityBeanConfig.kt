package com.nexusfi.server.infrastructure.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
class SecurityBeanConfig {

    // SecurityContextRepository를 별도 설정으로 분리하여 순환 참조 방지
    @Bean
    fun securityContextRepository(): SecurityContextRepository {
        return RequestAttributeSecurityContextRepository()
    }
}
