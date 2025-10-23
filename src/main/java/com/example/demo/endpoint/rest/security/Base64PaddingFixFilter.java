package com.example.demo.endpoint.rest.security;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(HIGHEST_PRECEDENCE)
public class Base64PaddingFixFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var originalParams = request.getParameterMap();

    var fixedParams =
        originalParams.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e ->
                        Arrays.stream(e.getValue())
                            .map(Base64PaddingFixFilter::padBase64IfNeeded)
                            .toArray(String[]::new)));

    var wrapped = new FixedParamsRequestWrapper(request, fixedParams);
    filterChain.doFilter(wrapped, response);
  }

  private static String padBase64IfNeeded(String s) {
    if (s == null) return null;
    int len = s.length();
    if (len == 0) return s;

    String base64UrlPattern = "^[A-Za-z0-9_\\-]+=*$";
    if (!s.matches(base64UrlPattern)) return s;

    int mod = len % 4;
    if (mod == 0) return s;

    int pad = 4 - mod;
    return s + "=".repeat(pad);
  }
}
