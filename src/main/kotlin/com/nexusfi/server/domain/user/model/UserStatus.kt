package com.nexusfi.server.domain.user.model

enum class UserStatus(
    val description: String
) {
    ACTIVE("활성"),
    DORMANT("휴면"), // 1년 이상 미접속 시
    DELETED("탈퇴")  // 탈퇴 요청 시 (데이터 보존 기간 후 삭제)
}
