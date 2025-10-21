package com.example.demo.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class LoggingAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private static final Logger log =
      LoggerFactory.getLogger(LoggingAuthorizationRequestResolver.class);

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

  public LoggingAuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    this.defaultResolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
    logState(authRequest);
    return authRequest;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
    logState(authRequest);
    return authRequest;
  }

  private void logState(OAuth2AuthorizationRequest authRequest) {
    if (authRequest != null) {
      log.info("🔹 Generated state (Spring Security) = {}", authRequest.getState());
    }
  }
}
