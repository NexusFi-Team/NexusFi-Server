package com.nexusfi.server.domain.transaction.repository

import com.nexusfi.server.domain.asset.model.AssetType
import com.nexusfi.server.domain.transaction.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// 거래 내역 데이터 접근을 위한 레포지토리
@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {
    // 특정 자산의 거래 내역 조회
    fun findAllByAssetIdAndAssetType(assetId: Long, assetType: AssetType): List<Transaction>
    
    // 사용자의 모든 거래 내역 조회 (최신순)
    fun findAllByEmailOrderByTransactionAtDesc(email: String): List<Transaction>
}
