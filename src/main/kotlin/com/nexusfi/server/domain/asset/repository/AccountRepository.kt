package com.nexusfi.server.domain.asset.repository

import com.nexusfi.server.domain.asset.model.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// 계좌 자산 데이터 접근을 위한 레포지토리
@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    // 사용자 이메일로 모든 계좌 조회
    fun findAllByEmail(email: String): List<Account>
}
