package com.example.jujuassembly.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiException extends RuntimeException {

  private String msg;
  private HttpStatus status;
}
