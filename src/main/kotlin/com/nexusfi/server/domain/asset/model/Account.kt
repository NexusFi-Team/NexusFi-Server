package com.nexusfi.server.domain.asset.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

// 은행 계좌 자산 엔티티
@Entity
@Table(name = "accounts")
class Account(
    email: String,
    name: String,
    amount: Long,

    // 은행 이름
    @Column(nullable = false)
    val bankName: String,

    // 계좌 번호
    @Column(nullable = false)
    val accountNumber: String
) : BaseAsset(email = email, name = name, amount = amount)
