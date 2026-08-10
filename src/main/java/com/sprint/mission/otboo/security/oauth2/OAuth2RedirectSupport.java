package com.sprint.mission.otboo.security.oauth2;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OAuth2RedirectSupport {

  private OAuth2RedirectSupport() {
  }

  public static void redirectWithError(HttpServletResponse response, String failureRedirectUri,
      String errorMessage) throws IOException {
    String separator = failureRedirectUri.contains("?") ? "&" : "?";
    String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    response.sendRedirect(
        failureRedirectUri + separator + "error=oauth_failed&error_message=" + encodedMessage);
  }
}
