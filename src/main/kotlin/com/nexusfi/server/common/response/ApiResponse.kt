package com.nexusfi.server.common.response

import com.nexusfi.server.common.exception.ErrorCode
import java.time.LocalDateTime

// 모든 API 응답의 공통 규격
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        // 성공 응답 생성
        fun <T> success(data: T?): ApiResponse<T> =
            ApiResponse(success = true, data = data)

        // 에러 응답 생성
        fun error(errorCode: ErrorCode): ApiResponse<Unit> =
            ApiResponse(
                success = false,
                error = ErrorDetail(
                    code = errorCode.code,
                    message = errorCode.message
                )
            )

        // 메시지 커스텀 에러 응답 생성
        fun error(errorCode: ErrorCode, message: String): ApiResponse<Unit> =
            ApiResponse(
                success = false,
                error = ErrorDetail(
                    code = errorCode.code,
                    message = message
                )
            )
    }

    data class ErrorDetail(
        val code: String,
        val message: String
    )
}
