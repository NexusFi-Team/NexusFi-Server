package com.nexusfi.server.common.exception

import com.nexusfi.server.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ApiResponse<Nothing> {
        return ApiResponse.error(e.message ?: "Internal Server Error", "SERVER_ERROR")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ApiResponse<Nothing> {
        return ApiResponse.error(e.message ?: "Invalid Argument", "INVALID_ARGUMENT")
    }
}
