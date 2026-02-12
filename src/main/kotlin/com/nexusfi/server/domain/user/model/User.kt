package com.nexusfi.server.domain.user.model

import com.nexusfi.server.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@IdClass(UserId::class)
class User(
    // 이메일 주소 (PK Part 1)
    @Id
    @Column(nullable = false)
    val email: String,

    // 소셜 로그인 제공자 (PK Part 2)
    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val socialType: SocialType,

    // 사용자 이름
    @Column(nullable = false)
    var name: String,

    // 소셜 제공자별 고유 ID
    @Column(nullable = false, unique = true)
    val socialId: String,

    // 생년월일 (추가 정보 입력 시 채워짐)
    @Column
    var birthDate: LocalDate? = null,

    // 성별 (추가 정보 입력 시 채워짐)
    @Enumerated(EnumType.STRING)
    @Column
    var gender: Gender? = Gender.UNKNOWN,

    // 회원 식별값 (CI)
    @Column
    var ci: String? = null,

    // 마지막 로그인 일시
    @Column
    var lastLoginAt: LocalDateTime? = null

) : BaseEntity() {

    // 소셜 정보 기반 프로필 업데이트
    fun updateProfile(name: String?): User {
        name?.let { this.name = it }
        return this
    }

    // 마지막 로그인 일시 업데이트
    fun updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now()
    }

    // 추가 정보 입력 완료 처리
    fun completeProfile(name: String?, birthDate: LocalDate, gender: Gender) {
        name?.let { this.name = it }
        this.birthDate = birthDate
        this.gender = gender
    }

    // 마이데이터 연동 성공 시 CI 저장
    fun linkedMyData(ci: String) {
        this.ci = ci
    }
}
