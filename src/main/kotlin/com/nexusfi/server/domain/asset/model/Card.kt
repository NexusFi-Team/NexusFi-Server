package com.nexusfi.server.domain.asset.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

// 신용/체크 카드 자산 엔티티
@Entity
@Table(name = "cards")
class Card(
    email: String,
    name: String,
    amount: Long,

    // 카드사 이름
    @Column(nullable = false)
    val cardCompany: String,

    // 카드 번호 (마스킹 처리 권장)
    @Column(nullable = false)
    val cardNumber: String
) : BaseAsset(email = email, name = name, amount = amount)
