package com.example.jujuassembly.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

  private String msg;
  private Integer statusCode;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  public ApiResponse(String msg, Integer statusCode) {
    this.msg = msg;
    this.statusCode = statusCode;
  }
}
