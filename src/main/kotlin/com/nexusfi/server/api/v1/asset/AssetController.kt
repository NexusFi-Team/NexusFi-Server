package com.nexusfi.server.api.v1.asset

import com.nexusfi.server.api.v1.asset.dto.AssetResponse
import com.nexusfi.server.application.asset.AssetService
import com.nexusfi.server.common.response.ApiResponse
import com.nexusfi.server.domain.user.model.UserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Asset API", description = "자산 관리 및 마이데이터 연동 API")
@RestController
@RequestMapping("/api/v1/assets")
class AssetController(
    private val assetService: AssetService
) {

    @Operation(summary = "내 자산 목록 조회", description = "현재 로그인한 사용자의 모든 자산(계좌, 카드, 대출)을 조회합니다.")
    @GetMapping
    suspend fun getMyAssets(
        @AuthenticationPrincipal userId: UserId
    ): ApiResponse<List<AssetResponse>> {
        // 복합 키의 이메일 정보를 사용하여 자산 조회
        val responses = assetService.getAllAssets(userId.email)
        
        return ApiResponse.success(responses)
    }

    @Operation(summary = "마이데이터 자산 연동 (시뮬레이션)", description = "가상의 마이데이터 연동을 통해 랜덤한 자산 데이터를 생성합니다.")
    @PostMapping("/link")
    suspend fun linkMyData(
        @AuthenticationPrincipal userId: UserId
    ): ApiResponse<Unit> {
        // 시뮬레이션 로직 실행 (랜덤 자산 생성 및 저장)
        assetService.linkMyData(userId.email)
        
        return ApiResponse.success(null, "성공적으로 자산이 연동되었습니다. 대시보드에서 확인해주세요!")
    }
}
