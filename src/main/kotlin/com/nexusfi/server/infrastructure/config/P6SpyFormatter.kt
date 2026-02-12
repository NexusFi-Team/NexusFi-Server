package com.nexusfi.server.infrastructure.config

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.P6SpyOptions
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.util.*

// P6Spy의 쿼리 로그 포맷을 한 줄로 최적화하는 포맷터
@Configuration
class P6SpyFormatter : MessageFormattingStrategy {

    // 빈이 생성된 후 P6Spy 옵션에 포맷터를 직접 등록
    @PostConstruct
    fun setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().logMessageFormat = this.javaClass.name
    }

    override fun formatMessage(
        connectionId: Int,
        now: String?,
        elapsed: Long,
        category: String?,
        prepared: String?,
        sql: String?,
        url: String?
    ): String {
        val formattedSql = formatSql(category, sql)
        // [카테고리] 실행시간ms | 쿼리 내용 형식으로 한 줄 출력
        return "[$category] ${elapsed}ms | $formattedSql"
    }

    private fun formatSql(category: String?, sql: String?): String? {
        if (sql.isNullOrBlank()) return sql

        // statement 카테고리인 경우에만 줄바꿈 제거 포맷팅 적용
        return if (Category.STATEMENT.name.equals(category, ignoreCase = true)) {
            // 줄바꿈 및 중복 공백 제거하여 한 줄로 만듦
            sql.replace(Regex("\\s+"), " ").trim()
        } else {
            sql
        }
    }
}
