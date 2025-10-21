package com.example.demo.endpoint.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StateFixFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var state = request.getParameter("state");
    var path = request.getRequestURI();

    if (state != null && path.contains("/login/oauth2/code/")) {
      var fixedState = padBase64(state);

      if (!fixedState.equals(state)) {

        var wrappedRequest =
            new HttpServletRequestWrapper(request) {
              @Override
              public String getParameter(String name) {
                if ("state".equals(name)) return fixedState;
                return super.getParameter(name);
              }

              @Override
              public Map<String, String[]> getParameterMap() {
                var map = new HashMap<>(super.getParameterMap());
                map.put("state", new String[] {fixedState});
                return map;
              }

              @Override
              public String[] getParameterValues(String name) {
                if ("state".equals(name)) return new String[] {fixedState};
                return super.getParameterValues(name);
              }
            };

        filterChain.doFilter(wrappedRequest, response);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private static String padBase64(String s) {
    if (s == null) return null;
    int mod = s.length() % 4;
    if (mod == 0) return s;
    int pad = 4 - mod;
    return s + "=".repeat(pad);
  }
}
