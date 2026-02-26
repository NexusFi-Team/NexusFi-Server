package com.nexusfi.server.api.v1.transaction

import com.nexusfi.server.api.v1.transaction.dto.TransactionResponse
import com.nexusfi.server.application.transaction.TransactionService
import com.nexusfi.server.common.response.ApiResponse
import com.nexusfi.server.domain.user.model.UserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Transaction API", description = "거래 내역 조회 API")
@RestController
@RequestMapping("/api/v1/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @Operation(summary = "이번 달 거래 내역 조회", description = "현재 로그인한 사용자의 이번 달 모든 거래 내역을 조회합니다.")
    @GetMapping("/me/current-month")
    fun getCurrentMonthTransactions(
        @AuthenticationPrincipal userId: UserId
    ): ApiResponse<List<TransactionResponse>> {
        val responses = transactionService.getCurrentMonthTransactions(userId.email)
        return ApiResponse.success(responses)
    }
}
