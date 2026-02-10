package com.nexusfi.server.application.user

import com.nexusfi.server.api.v1.user.dto.UserResponse
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    // 내 정보 조회
    fun getMyInfo(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        return UserResponse.from(user)
    }
}
