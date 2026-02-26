package com.nexusfi.server.domain.asset.model

import com.nexusfi.server.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

// 자산 도메인의 공통 속성을 정의하는 기본 클래스
@MappedSuperclass
abstract class BaseAsset(
    // 자산 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 자산 소유자 이메일
    @Column(nullable = false)
    val email: String,

    // 자산 이름
    @Column(nullable = false)
    val name: String,

    // 현재 잔액 또는 이용 금액
    @Column(nullable = false)
    val amount: Long
) : BaseEntity()
