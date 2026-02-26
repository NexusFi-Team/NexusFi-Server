package com.nexusfi.server.application.asset

import com.nexusfi.server.domain.asset.model.Account
import com.nexusfi.server.domain.asset.model.AssetType
import com.nexusfi.server.domain.asset.model.Card
import com.nexusfi.server.domain.asset.model.Loan
import org.springframework.stereotype.Component
import kotlin.random.Random

// 자산 엔티티 생성을 담당하는 팩토리 클래스
@Component
class AssetFactory {

    // 계좌 엔티티 생성
    fun createAccount(
        email: String,
        name: String,
        amount: Long,
        bankName: String,
        accountNumber: String
    ): Account {
        return Account(
            email = email,
            name = name,
            amount = amount,
            bankName = bankName,
            accountNumber = accountNumber
        )
    }

    // 카드 엔티티 생성
    fun createCard(
        email: String,
        name: String,
        amount: Long,
        cardCompany: String,
        cardNumber: String
    ): Card {
        return Card(
            email = email,
            name = name,
            amount = amount,
            cardCompany = cardCompany,
            cardNumber = cardNumber
        )
    }

    // 대출 엔티티 생성
    fun createLoan(
        email: String,
        name: String,
        amount: Long,
        provider: String,
        interestRate: Double
    ): Loan {
        return Loan(
            email = email,
            name = name,
            amount = amount,
            provider = provider,
            interestRate = interestRate
        )
    }

    // 마이데이터 시뮬레이션을 위한 랜덤 자산 생성 (통장)
    fun createRandomAccount(email: String): Account {
        val banks = listOf("신한은행", "국민은행", "우리은행", "하나은행", "NH농협")
        val bank = banks.random()
        return createAccount(
            email = email,
            name = "$bank 입출금 통장",
            // +1,000 ~ 1억 원 사이 랜덤
            amount = Random.nextLong(1000, 100_000_000),
            bankName = bank,
            accountNumber = "${Random.nextInt(100, 999)}-${Random.nextInt(100000, 999999)}"
        )
    }

    // 마이데이터 시뮬레이션을 위한 랜덤 자산 생성 (카드)
    fun createRandomCard(email: String): Card {
        val companies = listOf("현대카드", "삼성카드", "신한카드", "국민카드", "롯데카드")
        val company = companies.random()
        return createCard(
            email = email,
            name = "$company ZERO",
            // -100원 ~ -200만 원 사이 랜덤 (이용금액은 양수로 표현하고 나중에 계산)
            amount = Random.nextLong(100, 2_000_000),
            cardCompany = company,
            cardNumber = "****-****-****-${Random.nextInt(1000, 9999)}"
        )
    }

    // 마이데이터 시뮬레이션을 위한 랜덤 자산 생성 (대출)
    fun createRandomLoan(email: String): Loan {
        val providers = listOf("카카오뱅크", "케이뱅크", "토스뱅크", "현대캐피탈")
        val provider = providers.random()
        return createLoan(
            email = email,
            name = "$provider 신용대출",
            // 0원 ~ 1.5억 원 사이 랜덤
            amount = Random.nextLong(0, 150_000_000),
            provider = provider,
            interestRate = Random.nextDouble(3.5, 12.0)
        )
    }
}
