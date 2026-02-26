package com.nexusfi.server.domain.transaction.model

import io.swagger.v3.oas.annotations.media.Schema

// 거래의 종류 (입금/출금)
@Schema(description = "거래 타입")
enum class TransactionType(val description: String) {
    INCOME("입금"),
    EXPENSE("출금")
}

// 거래 카테고리
@Schema(description = "거래 카테고리")
enum class TransactionCategory(val description: String) {
    FOOD("식비"),
    SHOPPING("쇼핑"),
    TRANSPORT("교통"),
    HEALTH("의료"),
    LIVING("생활"),
    CULTURE("문화/취미"),
    SALARY("급여"),
    TRANSFER("이체"),
    OTHER("기타");

    // 카테고리별 랜덤 가맹점 추천 리스트
    fun getRandomMerchant(): String {
        return when (this) {
            FOOD -> listOf("스타벅스", "맥도날드", "김밥천국", "배달의민족", "서브웨이", "아웃백").random()
            SHOPPING -> listOf("쿠팡", "무신사", "네이버쇼핑", "올리브영", "현대백화점").random()
            TRANSPORT -> listOf("카카오택시", "지하철", "시내버스", "SK에너지", "쏘카").random()
            HEALTH -> listOf("서울대학병원", "올바른약국", "맑은안과", "튼튼치과").random()
            LIVING -> listOf("GS25", "CU", "다이소", "관리비", "쿠팡와우회비").random()
            CULTURE -> listOf("CGV", "넷플릭스", "교보문고", "멜론", "롯데시네마").random()
            SALARY -> listOf("회사급여", "상여금", "성과급").random()
            TRANSFER -> listOf("모영훈(이체)", "부모님", "적금자동이체").random()
            OTHER -> listOf("기타지출", "편의점", "현금결제").random()
        }
    }
}
