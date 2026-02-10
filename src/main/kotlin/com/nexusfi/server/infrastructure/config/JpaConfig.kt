package com.nexusfi.server.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

// JPA 관련 설정을 담당하는 클래스
@Configuration
@EnableJpaAuditing
class JpaConfig