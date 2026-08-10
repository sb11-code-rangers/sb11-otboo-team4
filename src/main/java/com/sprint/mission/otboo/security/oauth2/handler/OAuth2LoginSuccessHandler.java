package com.sprint.mission.otboo.security.oauth2.handler;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.service.OAuth2SignInService;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.OAuth2Provider;
import com.sprint.mission.otboo.security.cookie.provider.RefreshTokenCookieProvider;
import com.sprint.mission.otboo.security.oauth2.properties.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final OAuth2SignInService oAuth2SignInService;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;
  private final OAuth2Properties oAuth2Properties;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
    OAuth2User oAuth2User = token.getPrincipal();
    String registrationId = token.getAuthorizedClientRegistrationId();

    OAuth2Identity identity = resolveIdentity(registrationId, oAuth2User);

    SignInDto result = oAuth2SignInService.signIn(
        identity.provider(), identity.providerId(), identity.email(), identity.name(), null);

    refreshTokenCookieProvider.attach(response, result.refreshToken());
    response.sendRedirect(oAuth2Properties.successRedirectUri());
  }

  private OAuth2Identity resolveIdentity(String provider, OAuth2User oAuth2User) {
    return switch (provider) {
      case "google" -> new OAuth2Identity(
          OAuth2Provider.GOOGLE,
          oAuth2User.getAttribute("sub"),
          oAuth2User.getAttribute("email"),
          oAuth2User.getAttribute("name")
      );
      case "kakao" -> resolveKakaoIdentity(oAuth2User);
      default -> throw new IllegalStateException("지원하지 않는 provider: " + provider);
    };
  }

  // 카카오 id는 Long으로 내려오는데, String.valueOf(genericMethod())로 바로 넘기면
  // char[] 오버로드로 잘못 해석될 수 있어서(Object 변수로 한 번 받아서 넘겨야 한다.
  private OAuth2Identity resolveKakaoIdentity(OAuth2User oAuth2User) {
    Object rawId = oAuth2User.getAttribute("id");
    String providerId = String.valueOf(rawId);

    Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
    Map<String, Object> profile = kakaoAccount != null
        ? (Map<String, Object>) kakaoAccount.get("profile")
        : null;
    String nickname = profile != null ? (String) profile.get("nickname") : null;
    String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

    return new OAuth2Identity(OAuth2Provider.KAKAO, providerId, email, nickname);
  }

  private record OAuth2Identity(
      OAuth2Provider provider,
      String providerId,
      String email,
      String name
  ) {

  }
}
