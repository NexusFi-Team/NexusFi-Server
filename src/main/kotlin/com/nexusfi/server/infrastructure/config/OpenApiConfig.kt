package com.nexusfi.server.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
            .title("NexusFi API Documentation")
            .version("v1.0.0")
            .description("NexusFi 프로젝트의 API 명세서입니다.")

        // JWT 인증 설정
        val securitySchemeName = "jwtAuth"
        val securityRequirement = SecurityRequirement().addList(securitySchemeName)
        val components = Components()
            .addSecuritySchemes(
                securitySchemeName,
                SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )

        return OpenAPI()
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(components)
    }
}
