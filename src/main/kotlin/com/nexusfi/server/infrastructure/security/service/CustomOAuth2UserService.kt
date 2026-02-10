package com.nexusfi.server.infrastructure.security.service

import com.nexusfi.server.domain.user.model.SocialType
import com.nexusfi.server.domain.user.model.User
import com.nexusfi.server.domain.user.repository.UserRepository
import com.nexusfi.server.infrastructure.security.dto.CustomOAuth2User
import com.nexusfi.server.infrastructure.security.dto.OAuth2Attributes
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 소셜 로그인 성공 시 사용자 정보를 처리하는 서비스
@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // 소셜 서비스에서 유저 정보를 가져와 처리함
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        // 서비스 이름 (google, kakao 등) 획득
        val registrationId = userRequest.clientRegistration.registrationId
        val socialType = getSocialType(registrationId)
        
        // OAuth2 로그인 진행 시 키가 되는 필드값
        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
        
        // 소셜 유저 정보 파싱
        val attributes = OAuth2Attributes.of(socialType, userNameAttributeName, oAuth2User.attributes)

        // 유저 저장 또는 업데이트
        val user = saveOrUpdate(socialType, attributes)

        // SecurityContext에 담길 커스텀 유저 객체 반환
        return CustomOAuth2User(
            email = user.email,
            attributes = oAuth2User.attributes
        )
    }

    // 소셜 타입 매핑
    private fun getSocialType(registrationId: String): SocialType {
        return when (registrationId) {
            "kakao" -> SocialType.KAKAO
            "google" -> SocialType.GOOGLE
            else -> throw IllegalArgumentException("지원하지 않는 소셜 타입임")
        }
    }

    // 유저 정보 저장 또는 업데이트 로직
    private fun saveOrUpdate(socialType: SocialType, attributes: OAuth2Attributes): User {
        val user = userRepository.findBySocialTypeAndSocialId(socialType, attributes.oauth2UserInfo.getId())
            .map { 
                it.updateProfile(attributes.oauth2UserInfo.getNickname())
                // 로그인 시간 업데이트
                it.updateLastLoginAt()
                it 
            }
            .orElseGet { 
                val newUser = attributes.toEntity(socialType, attributes.oauth2UserInfo)
                // 최초 가입 시간도 로그인 시간으로 처리
                newUser.updateLastLoginAt()
                newUser
            }

        return userRepository.save(user)
    }
}
