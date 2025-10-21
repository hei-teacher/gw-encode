package com.example.demo.endpoint.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
  private final LoggingAuthorizationRequestResolver loggingResolver;
  private final String casdoorClientId;
  private final String casdoorLogoutUrl;

  public SecurityConfig(
      LoggingAuthorizationRequestResolver loggingResolver,
      @Value("${spring.security.oauth2.client.registration.casdoor.clientid}")
          String casdoorClientId,
      @Value("${casdoor.logout.url}") String casdoorLogoutUrl) {
    this.loggingResolver = loggingResolver;
    this.casdoorClientId = casdoorClientId;
    this.casdoorLogoutUrl = casdoorLogoutUrl;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(Customizer.withDefaults())
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/casdoor-logout")
                    .permitAll()
                    .requestMatchers("/")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth2 ->
                oauth2
                    .authorizationEndpoint(
                        auth -> auth.authorizationRequestResolver(loggingResolver))
                    .successHandler(
                        (request, response, authentication) -> {
                          String stateFromCallback = request.getParameter("state");
                          log.info("🔹 State received in callback = {}", stateFromCallback);

                          log.info("✅ OAuth2 login SUCCESS");
                          log.info("User: {}", authentication.getName());
                          log.info("Authorities: {}", authentication.getAuthorities());
                          response.sendRedirect("/welcome");
                        })
                    .failureHandler(
                        (request, response, exception) -> {
                          String stateFromCallback = request.getParameter("state");
                          log.info("🔹 State received in callback = {}", stateFromCallback);
                          log.error("❌ OAuth2 login FAILURE");
                          log.error("Message: {}", exception.getMessage());
                          new SimpleUrlAuthenticationFailureHandler("/oauth2/authorization/casdoor")
                              .onAuthenticationFailure(request, response, exception);
                          log.info("🔄 Forced redirect to /oauth2/authorization/casdoor executed");
                        }));

    return http.build();
  }
}
