package com.nexusfi.server.application.asset

import com.nexusfi.server.api.v1.asset.dto.AssetResponse
import com.nexusfi.server.application.transaction.TransactionService
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.asset.model.Account
import com.nexusfi.server.domain.asset.model.AssetType
import com.nexusfi.server.domain.asset.model.Card
import com.nexusfi.server.domain.asset.model.Loan
import com.nexusfi.server.domain.asset.repository.AccountRepository
import com.nexusfi.server.domain.asset.repository.CardRepository
import com.nexusfi.server.domain.asset.repository.LoanRepository
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import com.nexusfi.server.domain.user.model.UserStatus
import com.nexusfi.server.domain.user.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AssetServiceTest {

    private val accountRepository = mockk<AccountRepository>()
    private val cardRepository = mockk<CardRepository>()
    private val loanRepository = mockk<LoanRepository>()
    private val userRepository = mockk<UserRepository>()
    private val assetFactory = mockk<AssetFactory>()
    private val transactionService = mockk<TransactionService>()

    private val assetService = AssetService(
        accountRepository,
        cardRepository,
        loanRepository,
        userRepository,
        assetFactory,
        transactionService
    )

    private val email = "test@example.com"

    @Test
    @DisplayName("전체 자산 조회 성공 - 모든 타입의 자산을 합쳐서 반환한다")
    fun `getAllAssets success`() = runTest {
        // given
        val account = Account(email, "급여계좌", 1000, "신한은행", "110-123").setField("id", 1L)
        val card = Card(email, "국민카드", 500, "국민카드", "1234").setField("id", 2L)

        coEvery { accountRepository.findAllByEmail(email) } returns listOf(account)
        coEvery { cardRepository.findAllByEmail(email) } returns listOf(card)
        coEvery { loanRepository.findAllByEmail(email) } returns emptyList()

        // when
        val result = assetService.getAllAssets(email)

        // then
        assertEquals(2, result.size)
        assertEquals(AssetType.ACCOUNT, result[0].type)
        assertEquals(AssetType.CARD, result[1].type)
    }

    @Test
    @DisplayName("전체 자산 조회 실패 - 연동된 자산이 없는 경우 예외가 발생한다")
    fun `getAllAssets fail - no assets`() = runTest {
        // given
        coEvery { accountRepository.findAllByEmail(email) } returns emptyList()
        coEvery { cardRepository.findAllByEmail(email) } returns emptyList()
        coEvery { loanRepository.findAllByEmail(email) } returns emptyList()

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            // runTest 중첩 제거
            kotlinx.coroutines.runBlocking { assetService.getAllAssets(email) }
        }
        assertEquals(ErrorCode.ASSET_NOT_FOUND, exception.errorCode)
    }

    @Test
    @DisplayName("마이데이터 연동 성공 - 랜덤 자산을 생성하고 유저 상태를 LINKED로 변경한다")
    fun `linkMyData success`() = runTest {
        // given
        val user = User(email, SocialType.GOOGLE, "영훈", "social-id")
        val mockAccount = Account(email, "랜덤계좌", 1000, "은행", "123").setField("id", 1L)

        every { userRepository.findAll() } returns listOf(user)
        every { assetFactory.createRandomAccount(email) } returns mockAccount
        every { assetFactory.createRandomCard(email) } returns Card(email, "카드", 0, "회사", "1").setField("id", 2L)
        every { assetFactory.createRandomLoan(email) } returns Loan(email, "대출", 0, "은행", 5.0).setField("id", 3L)
        
        every { accountRepository.saveAll(any<Iterable<Account>>()) } returns listOf(mockAccount)
        every { cardRepository.saveAll(any<Iterable<Card>>()) } returns emptyList()
        every { loanRepository.saveAll(any<Iterable<Loan>>()) } returns emptyList()
        
        every { transactionService.generateSimulationData(any(), any(), any()) } returns Unit
        every { userRepository.save(user) } returns user

        // when
        assetService.linkMyData(email)

        // then
        assertEquals(UserStatus.LINKED, user.status)
        verify { userRepository.save(user) }
    }

    // 테스트를 위한 확장 함수: Reflection을 이용해 private 필드(id) 설정
    private fun <T : Any> T.setField(name: String, value: Any?): T {
        var clazz: Class<*>? = this.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(name)
                field.isAccessible = true
                field.set(this, value)
                return this
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw IllegalArgumentException("Field $name not found")
    }
}
