package com.nexusfi.server.application.transaction

import com.nexusfi.server.api.v1.transaction.dto.TransactionResponse
import com.nexusfi.server.domain.asset.model.AssetType
import com.nexusfi.server.domain.transaction.model.Transaction
import com.nexusfi.server.domain.transaction.model.TransactionCategory
import com.nexusfi.server.domain.transaction.model.TransactionType
import com.nexusfi.server.domain.transaction.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

// 거래 내역 비즈니스 로직 및 시뮬레이션 담당 서비스
@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {

    // 이번 달의 모든 거래 내역 조회
    @Transactional(readOnly = true)
    fun getCurrentMonthTransactions(email: String): List<TransactionResponse> {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN)
        val endOfMonth = now.with(LocalTime.MAX)

        return transactionRepository.findAllByEmailAndTransactionAtBetweenOrderByTransactionAtDesc(
            email, startOfMonth, endOfMonth
        ).map { TransactionResponse.from(it) }
    }

    // 특정 자산에 대해 최근 90일치 가상 거래 내역 생성
    @Transactional
    fun generateSimulationData(email: String, assetId: Long, assetType: AssetType) {
        val transactions = mutableListOf<Transaction>()
        val now = LocalDateTime.now()

        // 1. 최근 90일부터 오늘까지 하루씩 루프를 돌며 생성
        for (i in 0..90) {
            val date = now.minusDays(i.toLong())
            
            // 하루에 0~3건의 랜덤 거래 생성
            val dailyCount = Random.nextInt(0, 4)
            repeat(dailyCount) {
                val category = getRandomCategory(assetType)
                val type = if (category == TransactionCategory.SALARY) TransactionType.INCOME else TransactionType.EXPENSE
                
                transactions.add(
                    Transaction(
                        email = email,
                        assetId = assetId,
                        assetType = assetType,
                        amount = getRandomAmount(category),
                        transactionAt = date.withHour(Random.nextInt(8, 23)).withMinute(Random.nextInt(0, 60)),
                        merchantName = category.getRandomMerchant(),
                        category = category,
                        type = type
                    )
                )
            }
            
            // 매달 25일경에 급여 데이터 한 건 추가 (계좌인 경우만)
            if (assetType == AssetType.ACCOUNT && date.dayOfMonth == 25) {
                transactions.add(
                    Transaction(
                        email = email,
                        assetId = assetId,
                        assetType = assetType,
                        amount = Random.nextLong(2_500_000, 4_500_000),
                        transactionAt = date.withHour(10).withMinute(0),
                        merchantName = TransactionCategory.SALARY.getRandomMerchant(),
                        category = TransactionCategory.SALARY,
                        type = TransactionType.INCOME
                    )
                )
            }
        }

        // 2. 일괄 저장 (Batch 성능 고려)
        transactionRepository.saveAll(transactions)
    }

    // 카테고리에 따른 적절한 랜덤 금액 생성
    private fun getRandomAmount(category: TransactionCategory): Long {
        return when (category) {
            TransactionCategory.FOOD -> Random.nextLong(5_000, 50_000)
            TransactionCategory.SHOPPING -> Random.nextLong(10_000, 200_000)
            TransactionCategory.TRANSPORT -> Random.nextLong(1_250, 40_000)
            TransactionCategory.HEALTH -> Random.nextLong(3_000, 100_000)
            TransactionCategory.LIVING -> Random.nextLong(1_000, 150_000)
            TransactionCategory.CULTURE -> Random.nextLong(10_000, 80_000)
            else -> Random.nextLong(1_000, 30_000)
        }
    }

    // 자산 타입에 따른 랜덤 카테고리 추출
    private fun getRandomCategory(assetType: AssetType): TransactionCategory {
        val categories = if (assetType == AssetType.CARD) {
            listOf(TransactionCategory.FOOD, TransactionCategory.SHOPPING, TransactionCategory.TRANSPORT, TransactionCategory.CULTURE, TransactionCategory.LIVING)
        } else {
            listOf(TransactionCategory.FOOD, TransactionCategory.TRANSFER, TransactionCategory.LIVING, TransactionCategory.OTHER)
        }
        return categories.random()
    }
}
