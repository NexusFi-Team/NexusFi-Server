package com.nexusfi.server.api.v1.asset.dto

import com.nexusfi.server.domain.asset.model.AssetType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

// 자산 생성을 위한 요청 DTO
@Schema(description = "자산 생성 요청")
data class AssetCreateRequest(
    // 생성할 자산의 타입
    @field:NotNull(message = "자산 타입은 필수입니다.")
    @field:Schema(description = "자산 타입")
    val type: AssetType,

    // 자산 이름
    @field:NotBlank(message = "자산 이름은 필수입니다.")
    @field:Schema(description = "자산 이름", example = "현대카드 M")
    val name: String,

    // 초기 금액 또는 잔액
    @field:PositiveOrZero(message = "금액은 0 이상이어야 합니다.")
    @field:Schema(description = "금액", example = "0")
    val amount: Long,

    // 타입별 필수 추가 정보
    @field:Schema(description = "상세 정보", example = "{\"cardCompany\": \"현대카드\", \"cardNumber\": \"1234-****\"}")
    val additionalInfo: Map<String, Any> = emptyMap()
)
