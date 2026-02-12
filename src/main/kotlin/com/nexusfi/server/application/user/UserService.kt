package com.nexusfi.server.application.user

import com.nexusfi.server.api.v1.user.dto.UserProfileRequest
import com.nexusfi.server.api.v1.user.dto.UserResponse
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    // 내 정보 조회 (이메일과 소셜 타입을 조합하여 정확한 유저 조회)
    fun getMyInfo(email: String, socialType: SocialType): UserResponse {
        val user = userRepository.findByEmailAndSocialType(email, socialType)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        return UserResponse.from(user)
    }

    // 프로필 추가 정보 입력
    @Transactional
    fun completeProfile(email: String, socialType: SocialType, request: UserProfileRequest) {
        val user = userRepository.findByEmailAndSocialType(email, socialType)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 엔티티 내 비즈니스 메서드 호출
        user.completeProfile(
            name = request.name,
            birthDate = request.birthDate,
            gender = request.gender
        )
    }

    // 회원 탈퇴 (데이터 영구 삭제)
    @Transactional
    fun deleteUser(email: String, socialType: SocialType) {
        val user = userRepository.findByEmailAndSocialType(email, socialType)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        userRepository.delete(user)
    }
}
