package com.nexusfi.server.domain.user.model

import com.nexusfi.server.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val socialType: SocialType,

    @Column(nullable = false)
    val socialId: String,

    @Column
    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender? = Gender.UNKNOWN,

    // 회원 식별값 (CI - Connecting Information) - 마이데이터 연동 시 필수
    var ci: String? = null

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    /**
     * 추가 정보(프로필) 업데이트 로직
     * 첫 가입 후 정보가 없을 때 호출됨
     */
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