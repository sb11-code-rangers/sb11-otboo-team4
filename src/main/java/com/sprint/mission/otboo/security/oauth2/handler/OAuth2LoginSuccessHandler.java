package com.sprint.mission.otboo.security.oauth2.handler;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.service.OAuth2SignInService;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.OAuth2Provider;
import com.sprint.mission.otboo.security.cookie.provider.RefreshTokenCookieProvider;
import com.sprint.mission.otboo.security.oauth2.properties.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

// TODO: 지금은 구글만 지원한다. registrationId를 안 보고 무조건 GOOGLE/sub/email/name으로 읽는다.
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

    String providerId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    SignInDto result = oAuth2SignInService.signIn(OAuth2Provider.GOOGLE, providerId, email, name, null);

    refreshTokenCookieProvider.attach(response, result.refreshToken());
    response.sendRedirect(oAuth2Properties.successRedirectUri());
  }
}
