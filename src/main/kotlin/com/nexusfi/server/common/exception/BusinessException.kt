package com.nexusfi.server.common.exception

// 서비스 비즈니스 로직 중 발생하는 커스텀 예외의 최상위 클래스
open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)
