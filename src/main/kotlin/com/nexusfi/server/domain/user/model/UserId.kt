package com.nexusfi.server.domain.user.model

import java.io.Serializable

// User 엔티티의 복합 키를 위한 식별자 클래스
data class UserId(
    val email: String = "",
    val socialType: SocialType = SocialType.GOOGLE
) : Serializable
