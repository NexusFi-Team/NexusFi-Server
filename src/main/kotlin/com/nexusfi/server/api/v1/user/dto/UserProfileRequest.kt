package com.nexusfi.server.api.v1.user.dto

import com.nexusfi.server.domain.user.model.Gender
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Past
import java.time.LocalDate

@Schema(description = "프로필 추가 정보 입력 요청")
data class UserProfileRequest(
    @field:Schema(description = "사용자 이름 (닉네임 변경 시 사용)", example = "모영훈")
    val name: String? = null,

    @field:Past(message = "생년월일은 과거 날짜여야 합니다.")
    @field:Schema(description = "생년월일 (YYYY-MM-DD)", example = "1997-07-09")
    val birthDate: LocalDate,

    @field:Schema(description = "성별 (MALE, FEMALE)", example = "MALE")
    val gender: Gender
)
