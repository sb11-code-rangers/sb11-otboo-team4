package com.sprint.mission.otboo.security.oauth2.handler;

import com.sprint.mission.otboo.security.oauth2.OAuth2RedirectSupport;
import com.sprint.mission.otboo.security.oauth2.properties.OAuth2Properties;
import com.sprint.mission.otboo.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

  private static final String LINK_SUFFIX = "-link";

  private final OAuth2Properties oAuth2Properties;
  private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException {

    String errorMessage = (exception instanceof OAuth2AuthenticationException oAuth2Exception)
        ? oAuth2Exception.getError().getErrorCode()
        : "unknown_error";

    log.warn("OAuth2 로그인 실패: requestUri={}, reason={}", request.getRequestURI(), errorMessage,
        exception);

    String failureUri = isExplicitLink(request)
        ? oAuth2Properties.linkRedirectUri()
        : oAuth2Properties.failureRedirectUri();
    OAuth2RedirectSupport.redirectWithError(response, failureUri, errorMessage);
  }

  private boolean isExplicitLink(HttpServletRequest request) {
    OAuth2AuthorizationRequest authorizationRequest =
        authorizationRequestRepository.loadAuthorizationRequest(request);
    if (authorizationRequest == null) {
      return false;
    }
    Object registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
    return registrationId != null && registrationId.toString().endsWith(LINK_SUFFIX);
  }
}
