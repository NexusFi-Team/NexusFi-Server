package com.nexusfi.server.application.transaction

import com.nexusfi.server.domain.asset.model.AssetType
import com.nexusfi.server.domain.transaction.model.Transaction
import com.nexusfi.server.domain.transaction.model.TransactionCategory
import com.nexusfi.server.domain.transaction.model.TransactionType
import com.nexusfi.server.domain.transaction.repository.TransactionRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TransactionServiceTest {

    // relaxed = true를 사용하여 정의되지 않은 호출에 대해 기본값을 반환하도록 설정
    private val transactionRepository = mockk<TransactionRepository>(relaxed = true)
    private val transactionService = TransactionService(transactionRepository)

    private val email = "test@example.com"

    @Test
    @DisplayName("시뮬레이션 데이터 생성 성공 - 90일간의 데이터를 저장한다")
    fun `generateSimulationData success`() {
        // given
        val assetId = 1L
        val assetType = AssetType.ACCOUNT

        // when
        transactionService.generateSimulationData(email, assetId, assetType)

        // then
        // saveAll이 최소 한 번 이상 호출되었는지 검증
        verify(atLeast = 1) { transactionRepository.saveAll(any<Iterable<Transaction>>()) }
    }

    @Test
    @DisplayName("이번 달 거래 내역 조회 성공 - 현재 날짜 기준 해당 월의 데이터를 반환한다")
    fun `getCurrentMonthTransactions success`() {
        // given
        val now = LocalDateTime.now()
        val mockTransaction = Transaction(
            id = 1L,
            email = email,
            assetId = 1L,
            assetType = AssetType.CARD,
            amount = 10000,
            transactionAt = now,
            merchantName = "스타벅스",
            category = TransactionCategory.FOOD,
            type = TransactionType.EXPENSE
        )

        every { 
            transactionRepository.findAllByEmailAndTransactionAtBetweenOrderByTransactionAtDesc(
                eq(email), any(), any()
            ) 
        } returns listOf(mockTransaction)

        // when
        val result = transactionService.getCurrentMonthTransactions(email)

        // then
        assertEquals(1, result.size)
        assertEquals("스타벅스", result[0].merchantName)
    }
}
