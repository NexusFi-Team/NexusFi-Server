package com.nexusfi.server.api.v1.user.dto

import com.nexusfi.server.domain.user.model.Gender
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import java.time.LocalDate
import java.time.LocalDateTime

// 사용자 정보 응답 DTO
data class UserResponse(
    // 사용자 고유 식별자 ID
    val id: Long?,
    
    // 사용자 이메일 (로그인 계정)
    val email: String,
    
    // 사용자 이름 (닉네임)
    val name: String,
    
    // 소셜 로그인 제공자 (KAKAO, GOOGLE)
    val socialType: SocialType,
    
    // 생년월일 (YYYY-MM-DD)
    val birthDate: LocalDate?,
    
    // 성별 (MALE, FEMALE, UNKNOWN)
    val gender: Gender?,
    
    // 마지막 로그인 일시
    val lastLoginAt: LocalDateTime?,
    
    // 프로필 정보(생년월일, 성별) 완성 여부
    val isProfileCompleted: Boolean
) {
    companion object {
        // 엔티티를 DTO로 변환
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
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