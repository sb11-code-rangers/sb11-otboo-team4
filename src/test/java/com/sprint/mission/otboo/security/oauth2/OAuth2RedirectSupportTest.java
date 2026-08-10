package com.sprint.mission.otboo.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("OAuth2RedirectSupport")
class OAuth2RedirectSupportTest {

  @Nested
  @DisplayName("에러와 함께 리다이렉트 (redirectWithError)")
  class RedirectWithError {

    @Test
    @DisplayName("쿼리 파라미터가 없는 URI면 물음표로 에러를 붙인다")
    void 쿼리_파라미터가_없는_URI면_물음표로_에러를_붙인다() throws Exception {
      // given
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      OAuth2RedirectSupport.redirectWithError(response, "http://localhost:8080/#/", "login_required");

      // then
      assertThat(response.getRedirectedUrl())
          .isEqualTo("http://localhost:8080/#/?error=oauth_failed&error_message=login_required");
    }

    @Test
    @DisplayName("이미 쿼리 파라미터가 있는 URI면 앰퍼샌드로 에러를 붙인다")
    void 이미_쿼리_파라미터가_있는_URI면_앰퍼샌드로_에러를_붙인다() throws Exception {
      // given
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      OAuth2RedirectSupport.redirectWithError(
          response, "http://localhost:8080/#/?tab=login", "account_locked");

      // then
      assertThat(response.getRedirectedUrl())
          .isEqualTo("http://localhost:8080/#/?tab=login&error=oauth_failed&error_message=account_locked");
    }

    @Test
    @DisplayName("에러 메시지에 특수문자가 있으면 URL 인코딩해서 붙인다")
    void 에러_메시지에_특수문자가_있으면_URL_인코딩해서_붙인다() throws Exception {
      // given
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      OAuth2RedirectSupport.redirectWithError(
          response, "http://localhost:8080/#/", "already used & invalid");

      // then
      assertThat(response.getRedirectedUrl())
          .isEqualTo("http://localhost:8080/#/?error=oauth_failed&error_message=already+used+%26+invalid");
    }
  }
}
