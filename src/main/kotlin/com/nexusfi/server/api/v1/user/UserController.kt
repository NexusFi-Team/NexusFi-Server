package com.nexusfi.server.api.v1.user

import com.nexusfi.server.api.v1.user.dto.UserProfileRequest
import com.nexusfi.server.api.v1.user.dto.UserResponse
import com.nexusfi.server.application.auth.AuthService
import com.nexusfi.server.application.user.UserService
import com.nexusfi.server.common.response.ApiResponse
import com.nexusfi.server.common.utils.SecurityAudit
import com.nexusfi.server.domain.user.model.UserId
import com.nexusfi.server.infrastructure.utils.CookieUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "User API", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val authService: AuthService,
    private val cookieUtils: CookieUtils
) {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    suspend fun getMyInfo(@AuthenticationPrincipal userId: UserId): ApiResponse<UserResponse> {
        // 복합 키 정보를 사용하여 사용자 정보 조회
        val response = userService.getMyInfo(userId.email, userId.socialType)
        return ApiResponse.success(response)
    }

    @Operation(summary = "프로필 추가 정보 입력", description = "생년월일, 성별 등 가입 후 추가 정보를 입력합니다.")
    @PatchMapping("/profile")
    suspend fun completeProfile(
        @AuthenticationPrincipal userId: UserId,
        @Valid @RequestBody request: UserProfileRequest
    ): ApiResponse<Unit> {
        userService.completeProfile(userId.email, userId.socialType, request)
        return ApiResponse.success(null, "프로필 정보가 저장되었습니다.")
    }

    @Operation(summary = "회원 탈퇴", description = "사용자의 모든 데이터를 삭제하고 탈퇴 처리합니다.")
    @SecurityAudit("WITHDRAWAL")
    @DeleteMapping("/me")
    suspend fun withdraw(
        @AuthenticationPrincipal userId: UserId,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Unit> {
        // AccessToken 추출 (로그아웃과 동일하게 블랙리스트 처리 권장)
        val accessToken = request.getHeader("Authorization")?.substring(7)
            ?: ""

        // 1. DB 데이터 영구 삭제
        userService.deleteUser(userId.email, userId.socialType)

        // 2. Redis 토큰 삭제 및 블랙리스트 등록
        authService.logout(userId.email, accessToken)

        // 3. 쿠키 만료 처리
        val cookie = cookieUtils.deleteCookie("refreshToken")
        response.addHeader("Set-Cookie", cookie.toString())

        return ApiResponse.success(null, "회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.")
    }
}
