package com.example.demo.endpoint.rest.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class Base64PaddingFixFilterTest {

  private Base64PaddingFixFilter filter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;

  @BeforeEach
  void setUp() {
    filter = new Base64PaddingFixFilter();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    chain = mock(FilterChain.class);
  }

  @Test
  void base64_string_needs_padding_is_fixed() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("state", new String[] {"abc"}));

    assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

    var captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    verify(chain).doFilter(captor.capture(), eq(response));

    var wrappedRequest = captor.getValue();
    assertEquals("abc=", wrappedRequest.getParameter("state"));
  }

  @Test
  void base64_string_with_valid_padding_is_not_modified() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("state", new String[] {"abc="}));

    assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

    var captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    verify(chain).doFilter(captor.capture(), eq(response));

    var passedRequest = captor.getValue();
    assertEquals("abc=", passedRequest.getParameter("state"));
  }

  @Test
  void non_base64_value_is_not_modified() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("key", new String[] {"not@base64"}));

    assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

    var captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    verify(chain).doFilter(captor.capture(), eq(response));

    var passedRequest = captor.getValue();
    assertEquals("not@base64", passedRequest.getParameter("key"));
  }

  @Test
  void null_and_empty_values_are_handled_gracefully() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("null", new String[] {null}));

    assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

    var captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    verify(chain).doFilter(captor.capture(), eq(response));
    var wrapped = captor.getValue();

    assertNull(wrapped.getParameter("null"));
  }

  @Test
  void multiple_parameters_are_fixed_independently() throws ServletException, IOException {
    when(request.getParameterMap())
        .thenReturn(
            Map.of(
                "param1", new String[] {"ab"},
                "param2", new String[] {"TWFu"},
                "param3", new String[] {"not@base64"}));

    assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));

    var captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    verify(chain).doFilter(captor.capture(), eq(response));
    var wrapped = captor.getValue();

    assertEquals("ab==", wrapped.getParameter("param1"));
    assertEquals("TWFu", wrapped.getParameter("param2"));
    assertEquals("not@base64", wrapped.getParameter("param3"));
  }
}
