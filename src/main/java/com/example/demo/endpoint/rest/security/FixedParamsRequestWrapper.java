package com.example.demo.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

public class FixedParamsRequestWrapper extends HttpServletRequestWrapper {
  private final Map<String, String[]> fixedParams;

  public FixedParamsRequestWrapper(HttpServletRequest request, Map<String, String[]> fixedParams) {
    super(request);
    this.fixedParams = fixedParams;
  }

  @Override
  public String getParameter(String name) {
    var values = fixedParams.get(name);
    return (values != null && values.length > 0) ? values[0] : null;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return fixedParams;
  }

  @Override
  public String[] getParameterValues(String name) {
    return fixedParams.get(name);
  }
}
