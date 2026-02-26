package com.nexusfi.server.api.v1.transaction.dto

import com.nexusfi.server.domain.transaction.model.Transaction
import com.nexusfi.server.domain.transaction.model.TransactionCategory
import com.nexusfi.server.domain.transaction.model.TransactionType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "거래 내역 응답")
data class TransactionResponse(
    @field:Schema(description = "거래 ID", example = "1")
    val id: Long,

    @field:Schema(description = "거래 금액", example = "15000")
    val amount: Long,

    @field:Schema(description = "거래 일시", example = "2026-02-26T14:30:00")
    val transactionAt: LocalDateTime,

    @field:Schema(description = "가맹점명", example = "스타벅스 강남점")
    val merchantName: String,

    @field:Schema(description = "거래 카테고리")
    val category: TransactionCategory,

    @field:Schema(description = "거래 타입 (INCOME, EXPENSE)")
    val type: TransactionType
) {
    companion object {
        fun from(transaction: Transaction): TransactionResponse {
            return TransactionResponse(
                id = transaction.id!!,
                amount = transaction.amount,
                transactionAt = transaction.transactionAt,
                merchantName = transaction.merchantName,
                category = transaction.category,
                type = transaction.type
            )
        }
    }
}
