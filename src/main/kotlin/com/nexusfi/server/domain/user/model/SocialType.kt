package com.nexusfi.server.domain.user.model

enum class SocialType(
    val registrationId: String
) {
    KAKAO("kakao"),
    GOOGLE("google")
}