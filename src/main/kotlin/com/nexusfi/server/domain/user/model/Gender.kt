package com.nexusfi.server.domain.user.model

enum class Gender(
    val description: String
) {
    MALE("남성"),
    FEMALE("여성"),
    UNKNOWN("알 수 없음") // 기본값 또는 미입력 시 사용
}
