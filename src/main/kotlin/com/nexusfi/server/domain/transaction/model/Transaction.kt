package com.nexusfi.server.domain.transaction.model

import com.nexusfi.server.common.entity.BaseEntity
import com.nexusfi.server.domain.asset.model.AssetType
import jakarta.persistence.*
import java.time.LocalDateTime

// 모든 자산의 거래 내역을 관리하는 통합 엔티티
@Entity
@Table(
    name = "transactions",
    indexes = [
        Index(name = "idx_transaction_asset", columnList = "assetId, assetType"),
        Index(name = "idx_transaction_date", columnList = "transactionAt")
    ]
)
class Transaction(
    // 거래 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 소유자 이메일 (빠른 조회를 위해 추가)
    @Column(nullable = false)
    val email: String,

    // 관련 자산 ID (ID 참조 방식)
    @Column(nullable = false)
    val assetId: Long,

    // 관련 자산 타입 (ACCOUNT, CARD, LOAN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val assetType: AssetType,

    // 거래 금액
    @Column(nullable = false)
    val amount: Long,

    // 거래 일시
    @Column(nullable = false)
    val transactionAt: LocalDateTime,

    // 가맹점 이름 또는 적요
    @Column(nullable = false)
    val merchantName: String,

    // 거래 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: TransactionCategory,

    // 거래 타입 (INCOME, EXPENSE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType

) : BaseEntity()
