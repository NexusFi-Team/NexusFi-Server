package com.nexusfi.server.infrastructure.config

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import org.hibernate.engine.jdbc.internal.FormatStyle
import org.springframework.stereotype.Component
import java.util.*

@Component
class P6SpyFormatter : MessageFormattingStrategy {
    override fun formatMessage(
        connectionId: Int,
        now: String?,
        elapsed: Long,
        category: String?,
        prepared: String?,
        sql: String?,
        url: String?
    ): String {
        var formattedSql = sql
        if (formattedSql != null && formattedSql.trim().isNotEmpty() && Category.STATEMENT.name == category) {
            val trimmedSql = formattedSql.trim().lowercase(Locale.ROOT)
            formattedSql = if (trimmedSql.startsWith("create") || trimmedSql.startsWith("alter") || trimmedSql.startsWith("comment")) {
                FormatStyle.DDL.formatter.format(formattedSql)
            } else {
                FormatStyle.BASIC.formatter.format(formattedSql)
            }
        }
        return "\n[P6Spy] execution time: ${elapsed}ms | sql: $formattedSql"
    }
}