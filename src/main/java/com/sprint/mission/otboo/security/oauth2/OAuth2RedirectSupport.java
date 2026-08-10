package com.sprint.mission.otboo.security.oauth2;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2RedirectSupport {

  private OAuth2RedirectSupport() {
  }

  public static void redirectWithError(HttpServletResponse response, String failureRedirectUri,
      String errorMessage) throws IOException {
    String separator = failureRedirectUri.contains("?") ? "&" : "?";
    response.sendRedirect(
        failureRedirectUri + separator + "error=oauth_failed&error_message=" + errorMessage);
  }
}
