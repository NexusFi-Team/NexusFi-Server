package com.nexusfi.server.common.exception

import com.nexusfi.server.common.response.ApiResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    // 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        log.error("BusinessException: ${e.errorCode.message}", e)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ApiResponse.error(e.errorCode))
    }

    // @Valid 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        log.error("MethodArgumentNotValidException", e)
        val message = e.bindingResult.fieldError?.defaultMessage ?: ErrorCode.INVALID_INPUT_VALUE.message
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, message))
    }

    // @Validated 검증 예외 처리 (메서드 파라미터 등)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<ApiResponse<Unit>> {
        log.error("ConstraintViolationException", e)
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, e.message ?: ErrorCode.INVALID_INPUT_VALUE.message))
    }

    // 필수 쿼리 파라미터 누락 예외 처리
    @ExceptionHandler(value = [MissingServletRequestParameterException::class])
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Unit>> {
        log.error("MissingServletRequestParameterException", e)
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, "${e.parameterName} 파라미터가 누락되었습니다."))
    }

    // 메서드 인자 타입 불일치 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        log.error("MethodArgumentTypeMismatchException", e)
        return ResponseEntity
            .status(ErrorCode.INVALID_TYPE_VALUE.status)
            .body(ApiResponse.error(ErrorCode.INVALID_TYPE_VALUE))
    }

    // 지원하지 않는 HTTP 메서드 호출 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Unit>> {
        log.error("HttpRequestMethodNotSupportedException", e)
        return ResponseEntity
            .status(ErrorCode.METHOD_NOT_ALLOWED.status)
            .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED))
    }

    // 엔티티 미조회 예외 처리
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<ApiResponse<Unit>> {
        log.error("EntityNotFoundException", e)
        return ResponseEntity
            .status(ErrorCode.ENTITY_NOT_FOUND.status)
            .body(ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND))
    }

    // DB 데이터 무결성 위반 예외 처리 (Unique Key 중복 등)
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): ResponseEntity<ApiResponse<Unit>> {
        log.error("DataIntegrityViolationException", e)
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, "데이터 무결성 제약 조건을 위반했습니다."))
    }

    // 권한 부족 예외 처리
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ApiResponse<Unit>> {
        log.error("AccessDeniedException", e)
        return ResponseEntity
            .status(ErrorCode.HANDLE_ACCESS_DENIED.status)
            .body(ApiResponse.error(ErrorCode.HANDLE_ACCESS_DENIED))
    }

    // JSON 파싱 에러 처리
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit>> {
        log.error("HttpMessageNotReadableException", e)
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE))
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("Unhandled Exception: ${e.message}", e)
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR))
    }
}
