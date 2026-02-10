package com.nexusfi.server.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
) {
    companion object {
        fun <T> success(data: T?): ApiResponse<T> = ApiResponse(true, data)
        fun error(message: String, code: String): ApiResponse<Nothing> = 
            ApiResponse(false, null, ApiError(message, code))
    }
}

data class ApiError(
    val message: String,
    val code: String
)
