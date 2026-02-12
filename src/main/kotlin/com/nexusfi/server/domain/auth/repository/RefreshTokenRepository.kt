package com.nexusfi.server.domain.auth.repository

import com.nexusfi.server.domain.auth.RefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

// 리프레시 토큰 저장을 위한 레포지토리
@Repository
interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
    
    // 토큰 값으로 데이터 조회
    fun findByToken(token: String): RefreshToken?
}
