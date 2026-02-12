package com.nexusfi.server.domain.user.repository

import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import com.nexusfi.server.domain.user.model.UserId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UserId> {
    
    // 이메일로 사용자 목록 조회
    fun findByEmail(email: String): List<User>

    // 특정 소셜 타입의 이메일로 사용자 조회 (단건)
    fun findByEmailAndSocialType(email: String, socialType: SocialType): Optional<User>

    // 소셜 타입과 소셜 ID로 사용자 조회 (OAuth2 로그인 시 사용)
    fun findBySocialTypeAndSocialId(socialType: SocialType, socialId: String): Optional<User>

    // 이메일 존재 여부 체크
    fun existsByEmail(email: String): Boolean
}
