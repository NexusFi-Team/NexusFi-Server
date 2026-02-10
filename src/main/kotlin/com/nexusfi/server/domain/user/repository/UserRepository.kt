package com.nexusfi.server.domain.user.repository

import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    
    // 이메일로 사용자 조회 (로그인 시 사용)
    fun findByEmail(email: String): Optional<User>

    // 소셜 타입과 소셜 ID로 사용자 조회 (OAuth2 로그인 시 사용)
    fun findBySocialTypeAndSocialId(socialType: SocialType, socialId: String): Optional<User>

    // 이메일 중복 체크
    fun existsByEmail(email: String): Boolean
}
