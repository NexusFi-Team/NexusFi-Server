package com.nexusfi.server.api.v1.user

import com.nexusfi.server.api.v1.user.dto.UserResponse
import com.nexusfi.server.application.user.UserService
import com.nexusfi.server.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User API", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    fun getMyInfo(@AuthenticationPrincipal email: String): ApiResponse<UserResponse> {
        val response = userService.getMyInfo(email)
        return ApiResponse.success(response)
    }
}
