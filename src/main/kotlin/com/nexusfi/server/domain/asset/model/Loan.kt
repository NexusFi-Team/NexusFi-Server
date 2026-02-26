package com.nexusfi.server.domain.asset.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

// 대출 자산 엔티티
@Entity
@Table(name = "loans")
class Loan(
    email: String,
    name: String,
    amount: Long,

    // 대출 제공 기관
    @Column(nullable = false)
    val provider: String,

    // 대출 이자율
    @Column(nullable = false)
    val interestRate: Double
) : BaseAsset(email = email, name = name, amount = amount)
