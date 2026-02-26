package com.nexusfi.server.api.v1.asset.dto

import com.nexusfi.server.domain.asset.model.AssetType
import io.swagger.v3.oas.annotations.media.Schema

// 모든 자산 정보를 통합하여 응답하는 DTO
@Schema(description = "자산 통합 응답")
data class AssetResponse(
    // 자산 고유 식별자
    @field:Schema(description = "자산 ID", example = "1")
    val id: Long,

    // 자산 종류 (ACCOUNT, CARD, LOAN)
    @field:Schema(description = "자산 타입 (ACCOUNT: 계좌, CARD: 카드, LOAN: 대출)")
    val type: AssetType,

    // 사용자가 지정한 자산 이름
    @field:Schema(description = "자산 이름", example = "생활비 통장")
    val name: String,

    // 현재 잔액 또는 이용 금액
    @field:Schema(description = "금액", example = "1500000")
    val amount: Long,

    // 자산 타입별 상세 정보
    @field:Schema(
        description = "자산 타입별 상세 정보 (Map 형태)", 
        example = "{\"bankName\": \"신한은행\", \"accountNumber\": \"110-123-...\", \"cardCompany\": \"현대카드\", \"interestRate\": 4.5}"
    )
    val additionalInfo: Map<String, Any>
)
