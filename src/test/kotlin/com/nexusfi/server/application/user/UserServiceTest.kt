package com.nexusfi.server.application.user

import com.nexusfi.server.api.v1.user.dto.UserProfileRequest
import com.nexusfi.server.common.exception.BusinessException
import com.nexusfi.server.common.exception.ErrorCode
import com.nexusfi.server.domain.user.model.Gender
import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import com.nexusfi.server.domain.user.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)

    private val email = "test@example.com"
    private val socialType = SocialType.GOOGLE
    private val socialId = "google-12345"

    @Test
    @DisplayName("내 정보 조회 성공 - 유저 정보를 반환한다")
    fun `getMyInfo success`() = runTest {
        // given
        val user = User(
            email = email,
            socialType = socialType,
            name = "영훈",
            socialId = socialId
        )
        every { userRepository.findByEmailAndSocialType(email, socialType) } returns Optional.of(user)

        // when
        val result = userService.getMyInfo(email, socialType)

        // then
        assertEquals(email, result.email)
        assertEquals("영훈", result.name)
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 유저가 없는 경우 예외가 발생한다")
    fun `getMyInfo fail - user not found`() = runTest {
        // given
        every { userRepository.findByEmailAndSocialType(email, socialType) } returns Optional.empty()

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            kotlinx.coroutines.runBlocking { userService.getMyInfo(email, socialType) }
        }
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
    }

    @Test
    @DisplayName("프로필 추가 정보 입력 성공 - 유저 정보를 업데이트한다")
    fun `completeProfile success`() = runTest {
        // given
        val user = User(
            email = email,
            socialType = socialType,
            name = "영훈",
            socialId = socialId
        )
        val request = UserProfileRequest(
            name = "새이름",
            birthDate = LocalDate.of(1995, 1, 1),
            gender = Gender.MALE
        )

        every { userRepository.findByEmailAndSocialType(email, socialType) } returns Optional.of(user)

        // when
        userService.completeProfile(email, socialType, request)

        // then
        assertEquals("새이름", user.name)
        assertEquals(request.birthDate, user.birthDate)
        assertEquals(request.gender, user.gender)
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 유저 데이터를 삭제한다")
    fun `deleteUser success`() = runTest {
        // given
        val user = User(
            email = email,
            socialType = socialType,
            name = "영훈",
            socialId = socialId
        )
        every { userRepository.findByEmailAndSocialType(email, socialType) } returns Optional.of(user)
        every { userRepository.delete(user) } returns Unit

        // when
        userService.deleteUser(email, socialType)

        // then
        verify { userRepository.delete(user) }
    }
}
