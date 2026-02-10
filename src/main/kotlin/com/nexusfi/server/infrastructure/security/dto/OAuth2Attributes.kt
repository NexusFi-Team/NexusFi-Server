package com.nexusfi.server.infrastructure.security.dto

import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User

// 소셜 서비스별 유저 정보를 공통 규격으로 변환하는 DTO
data class OAuth2Attributes(
    // OAuth2 로그인 식별자 키 (구글: sub, 카카오: id)
    val nameAttributeKey: String,
    // 실제 사용자 정보 객체
    val oauth2UserInfo: OAuth2UserInfo
) {
    companion object {
        // 소셜 타입에 따른 OAuth2Attributes 객체 생성
        fun of(
            socialType: SocialType,
            userNameAttributeName: String,
            attributes: Map<String, Any>
        ): OAuth2Attributes {
            return when (socialType) {
                SocialType.KAKAO -> ofKakao(userNameAttributeName, attributes)
                SocialType.GOOGLE -> ofGoogle(userNameAttributeName, attributes)
            }
        }

        // 구글 정보 추출
        private fun ofGoogle(userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Attributes {
            return OAuth2Attributes(
                nameAttributeKey = userNameAttributeName,
                oauth2UserInfo = GoogleUserInfo(attributes)
            )
        }

        // 카카오 정보 추출
        private fun ofKakao(userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Attributes {
            return OAuth2Attributes(
                nameAttributeKey = userNameAttributeName,
                oauth2UserInfo = KakaoUserInfo(attributes)
            )
        }
    }

    // 최초 가입용 User 엔티티 변환
    fun toEntity(socialType: SocialType, oauth2UserInfo: OAuth2UserInfo): User {
        return User(
            // 이메일 미제공 시 빈 문자열 처리
            email = oauth2UserInfo.getEmail() ?: "",
            // 닉네임 미제공 시 기본값 설정
            name = oauth2UserInfo.getNickname() ?: "User",
            socialType = socialType,
            socialId = oauth2UserInfo.getId(),
            // CI 정보는 소셜 가입 시점엔 거의 없으므로 null 허용 필드에 저장
            ci = oauth2UserInfo.getCi()
        )
    }
}

// 소셜 서비스별 유저 정보 인터페이스
interface OAuth2UserInfo {
    fun getId(): String
    fun getNickname(): String?
    fun getEmail(): String?
    // 실명 인증 식별값(CI) 추출 로직 정의
    fun getCi(): String?
}

// 구글 유저 정보 구현체
class GoogleUserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {
    override fun getId(): String = attributes["sub"] as String
    override fun getNickname(): String? = attributes["name"] as? String
    override fun getEmail(): String? = attributes["email"] as? String
    // 구글은 기본적으로 CI를 제공하지 않음
    override fun getCi(): String? = null
}

// 카카오 유저 정보 구현체
class KakaoUserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {
    override fun getId(): String = attributes["id"].toString()
    
    override fun getNickname(): String? {
        val properties = attributes["properties"] as? Map<*, *>
        return properties?.get("nickname") as? String
    }

    override fun getEmail(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        return kakaoAccount?.get("email") as? String
    }

    // 카카오 비즈니스 채널 설정을 통해 CI를 제공받는 경우 사용 가능
    override fun getCi(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        return kakaoAccount?.get("ci") as? String
    }
}