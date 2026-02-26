package com.nexusfi.server.domain.asset.model

import io.swagger.v3.oas.annotations.media.Schema

// 자산의 종류를 정의하는 열거형
@Schema(description = "자산 타입")
enum class AssetType(val description: String) {
    // 은행 계좌
    ACCOUNT("계좌"),
    
    // 신용/체크 카드
    CARD("카드"),
    
    // 대출
    LOAN("대출")
}
