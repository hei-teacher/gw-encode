package com.example.demo.endpoint.rest.security;

import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

  private final String casdoorClientId;
  private final String casdoorLogoutUrl;
  private final Base64PaddingFixFilter statePaddingFixFilter;

  public SecurityConfig(
      @Value("${spring.security.oauth2.client.registration.casdoor.clientid}")
          String casdoorClientId,
      @Value("${casdoor.logout.url}") String casdoorLogoutUrl,
      Base64PaddingFixFilter statePaddingFixFilter) {
    this.casdoorClientId = casdoorClientId;
    this.casdoorLogoutUrl = casdoorLogoutUrl;
    this.statePaddingFixFilter = statePaddingFixFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/casdoor-logout")
                    .permitAll()
                    .requestMatchers("/")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(statePaddingFixFilter, BasicAuthenticationFilter.class)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .successHandler(
                        (request, response, authentication) -> {
                          log.info("✅ OAuth2 login SUCCESS");
                          log.info("User: {}", authentication.getName());
                          log.info("Authorities: {}", authentication.getAuthorities());
                          response.sendRedirect("/welcome");
                        })
                    .failureHandler(
                        (request, response, exception) -> {
                          log.error("❌ OAuth2 login FAILURE");
                          log.error("Message: {}", exception.getMessage());

                          Map<String, String[]> parameterMap = request.getParameterMap();
                          if (parameterMap.isEmpty()) {
                            log.warn("⚠️ No parameters found in the request.");
                          } else {
                            parameterMap.forEach(
                                (key, values) ->
                                    log.info("Parameter [{}] = {}", key, Arrays.toString(values)));
                          }

                          new SimpleUrlAuthenticationFailureHandler("/oauth2/authorization/casdoor")
                              .onAuthenticationFailure(request, response, exception);
                          log.info("🔄 Forced redirect to /oauth2/authorization/casdoor executed");
                        }));

    return http.build();
  }
}
