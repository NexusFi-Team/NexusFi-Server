package com.nexusfi.server.domain.asset.repository

import com.nexusfi.server.domain.asset.model.Card
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// 카드 자산 데이터 접근을 위한 레포지토리
@Repository
interface CardRepository : JpaRepository<Card, Long> {
    // 사용자 이메일로 모든 카드 조회
    fun findAllByEmail(email: String): List<Card>
}
