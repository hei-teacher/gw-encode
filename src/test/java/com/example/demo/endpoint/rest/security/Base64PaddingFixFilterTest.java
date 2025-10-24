package com.example.demo.endpoint.rest.security;

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
  private ArgumentCaptor<HttpServletRequest> captor;

  @BeforeEach
  void setUp() {
    filter = new Base64PaddingFixFilter();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    chain = mock(FilterChain.class);
    captor = ArgumentCaptor.forClass(HttpServletRequest.class);
  }

  @Test
  void should_add_padding_to_base64_without_padding() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("state", new String[] {"abc"}));

    var wrappedRequest = runFilterAndCapture();

    assertEquals("abc=", wrappedRequest.getParameter("state"));
  }

  @Test
  void should_not_modify_base64_with_valid_padding() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("state", new String[] {"abc="}));

    var wrappedRequest = runFilterAndCapture();

    assertEquals("abc=", wrappedRequest.getParameter("state"));
  }

  @Test
  void should_not_modify_non_base64_values() throws ServletException, IOException {
    when(request.getParameterMap()).thenReturn(Map.of("key", new String[] {"not@base64"}));

    var wrappedRequest = runFilterAndCapture();

    assertEquals("not@base64", wrappedRequest.getParameter("key"));
  }

  @Test
  void should_handle_null_and_empty_parameters_gracefully() throws ServletException, IOException {
    when(request.getParameterMap())
        .thenReturn(Map.of("empty", new String[] {""}, "null", new String[] {null}));

    var wrappedRequest = runFilterAndCapture();

    assertEquals("", wrappedRequest.getParameter("empty"));
    assertNull(wrappedRequest.getParameter("null"));
  }

  @Test
  void should_fix_multiple_parameters_independently() throws ServletException, IOException {
    when(request.getParameterMap())
        .thenReturn(
            Map.of(
                "param1", new String[] {"ab"},
                "param2", new String[] {"TWFu"},
                "param3", new String[] {"not@base64"}));

    var wrappedRequest = runFilterAndCapture();

    assertEquals("ab==", wrappedRequest.getParameter("param1"));
    assertEquals("TWFu", wrappedRequest.getParameter("param2"));
    assertEquals("not@base64", wrappedRequest.getParameter("param3"));
  }

  private HttpServletRequest runFilterAndCapture() throws ServletException, IOException {
    filter.doFilterInternal(request, response, chain);
    verify(chain).doFilter(captor.capture(), eq(response));
    return captor.getValue();
  }
}
