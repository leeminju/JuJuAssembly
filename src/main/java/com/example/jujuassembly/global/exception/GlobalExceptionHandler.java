package com.example.jujuassembly.global.exception;

import com.example.jujuassembly.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<ApiResponse> IllegalArgumentExceptionHandler(IllegalArgumentException ex) {
    ApiResponse commonResponseDto = new ApiResponse(ex.getMessage(),
        HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(commonResponseDto, HttpStatus.BAD_REQUEST);
  }

}
