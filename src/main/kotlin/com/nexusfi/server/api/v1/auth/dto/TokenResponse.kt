package com.nexusfi.server.api.v1.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 재발급 응답")
data class TokenResponse(
    @field:Schema(description = "새로운 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String
)
