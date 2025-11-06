package com.example.demo.endpoint.rest.security;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(HIGHEST_PRECEDENCE)
public class Base64PaddingFixFilter extends OncePerRequestFilter {
  private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]*={0,2}$");

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

  public static boolean isProbablyBase64(String s) {
    if (s == null || s.isEmpty()) return false;

    if (!BASE64_PATTERN.matcher(s).matches()) return false;

    var padCount = (4 - (s.length() % 4)) % 4;
    var padded = s + "=".repeat(padCount);

    try {
      var decoded = Base64.getDecoder().decode(padded);

      var reencoded = Base64.getEncoder().encodeToString(decoded);

      var origNorm = padded.replaceAll("=+$", "");
      var reencNorm = reencoded.replaceAll("=+$", "");

      return origNorm.equals(reencNorm);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static String padBase64IfNeeded(String s) {
    if (s == null || s.isEmpty()) return s;
    if (!isProbablyBase64(s)) return s;

    var padCount = (4 - (s.length() % 4)) % 4;
    if (padCount == 0) return s;
    return s + "=".repeat(padCount);
  }
}
