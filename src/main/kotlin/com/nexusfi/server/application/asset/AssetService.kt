package com.nexusfi.server.application.asset

import com.nexusfi.server.api.v1.asset.dto.AssetResponse
import com.nexusfi.server.application.transaction.TransactionService
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.asset.model.AssetType
import com.nexusfi.server.domain.asset.repository.AccountRepository
import com.nexusfi.server.domain.asset.repository.CardRepository
import com.nexusfi.server.domain.asset.repository.LoanRepository
import com.nexusfi.server.domain.user.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 자산 관리 비즈니스 로직을 처리하는 서비스
@Service
class AssetService(
    private val accountRepository: AccountRepository,
    private val cardRepository: CardRepository,
    private val loanRepository: LoanRepository,
    private val userRepository: UserRepository,
    private val assetFactory: AssetFactory,
    private val transactionService: TransactionService
) {

    // 전체 자산 목록 조회 (병렬 처리 적용)
    @Transactional(readOnly = true)
    suspend fun getAllAssets(email: String): List<AssetResponse> = coroutineScope {
        // 1. 각 자산 테이블을 비동기 병렬로 조회 (Virtual Threads가 blocking I/O를 효율적으로 처리)
        val accountsDeferred = async { accountRepository.findAllByEmail(email) }
        val cardsDeferred = async { cardRepository.findAllByEmail(email) }
        val loansDeferred = async { loanRepository.findAllByEmail(email) }

        // 2. 결과 대기 및 통합
        val accounts = accountsDeferred.await()
        val cards = cardsDeferred.await()
        val loans = loansDeferred.await()

        // 3. 연동된 자산이 하나도 없는 경우 예외 발생
        if (accounts.isEmpty() && cards.isEmpty() && loans.isEmpty()) {
            throw BusinessException(ErrorCode.ASSET_NOT_FOUND)
        }

        // 4. DTO 변환 및 통합
        val accountResponses = accounts.map {
            AssetResponse(
                id = it.id!!,
                type = AssetType.ACCOUNT,
                name = it.name,
                amount = it.amount,
                additionalInfo = mapOf("bankName" to it.bankName, "accountNumber" to it.accountNumber)
            )
        }

        val cardResponses = cards.map {
            AssetResponse(
                id = it.id!!,
                type = AssetType.CARD,
                name = it.name,
                amount = it.amount,
                additionalInfo = mapOf("cardCompany" to it.cardCompany, "cardNumber" to it.cardNumber)
            )
        }

        val loanResponses = loans.map {
            AssetResponse(
                id = it.id!!,
                type = AssetType.LOAN,
                name = it.name,
                amount = it.amount,
                additionalInfo = mapOf("provider" to it.provider, "interestRate" to it.interestRate)
            )
        }

        accountResponses + cardResponses + loanResponses
    }

    // 마이데이터 연동 시뮬레이션 (랜덤 자산 생성)
    @Transactional
    suspend fun linkMyData(email: String) {
        // 1. 사용자 존재 확인 및 상태 업데이트를 위한 조회
        val user = userRepository.findAll().find { it.email == email }
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        // 2. 랜덤 자산 세트 생성
        val newAccounts = List(RandomInfo.count(1, 3)) { assetFactory.createRandomAccount(email) }
        val newCards = List(RandomInfo.count(1, 2)) { assetFactory.createRandomCard(email) }
        val newLoans = if (Math.random() > 0.5) listOf(assetFactory.createRandomLoan(email)) else emptyList()

        // 3. 각 레포지토리에 저장
        val savedAccounts = accountRepository.saveAll(newAccounts)
        val savedCards = cardRepository.saveAll(newCards)
        val savedLoans = loanRepository.saveAll(newLoans)

        // 4. 저장된 자산별로 거래 내역 시뮬레이션 데이터 생성
        savedAccounts.forEach { transactionService.generateSimulationData(email, it.id!!, AssetType.ACCOUNT) }
        savedCards.forEach { transactionService.generateSimulationData(email, it.id!!, AssetType.CARD) }
        savedLoans.forEach { transactionService.generateSimulationData(email, it.id!!, AssetType.LOAN) }

        // 5. 사용자 상태를 '연동완료'로 업데이트
        user.updateStatusToLinked()
        userRepository.save(user)
    }

    // 내부 유틸리티: 랜덤 개수 결정을 위한 헬퍼
    private object RandomInfo {
        fun count(min: Int, max: Int): Int = (min..max).random()
    }
}
