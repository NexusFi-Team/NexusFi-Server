package com.nexusfi.server.api.v1.user.dto

import com.nexusfi.server.domain.user.model.Gender
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "사용자 정보 응답")
data class UserResponse(
    @field:Schema(description = "사용자 이메일 (로그인 계정)", example = "example@nexusfi.com")
    val email: String,
    
    @field:Schema(description = "사용자 이름 (닉네임)", example = "모영훈")
    var name: String,
    
    @field:Schema(description = "소셜 로그인 제공자", example = "KAKAO")
    val socialType: SocialType,
    
    @field:Schema(description = "생년월일 (YYYY-MM-DD)", example = "1997-07-09")
    val birthDate: LocalDate?,
    
    @field:Schema(description = "성별 (MALE, FEMALE)", example = "MALE")
    val gender: Gender?,
    
    @field:Schema(description = "마지막 로그인 일시", example = "2026-02-10T12:00:00")
    val lastLoginAt: LocalDateTime?,
    
    @field:Schema(description = "프로필 정보 완성 여부", example = "false")
    val isProfileCompleted: Boolean
) {
    companion object {
        // 엔티티를 DTO로 변환
        fun from(user: User): UserResponse {
            return UserResponse(
                email = user.email,
                name = user.name,
                socialType = user.socialType,
                birthDate = user.birthDate,
                gender = user.gender,
                lastLoginAt = user.lastLoginAt,
                isProfileCompleted = user.birthDate != null && user.gender != null
            )
        }
    }
}
