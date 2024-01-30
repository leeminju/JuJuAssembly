package com.example.jujuassembly.global.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class TokenWrapperRequest extends HttpServletRequestWrapper {

  private final String accessToken;

  public TokenWrapperRequest(HttpServletRequest request, String accessToken) {
    super(request);
    this.accessToken = accessToken;
  }

  @Override
  public String getHeader(String name) {

    return super.getHeader(name);
  }
}
