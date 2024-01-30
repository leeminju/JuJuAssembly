package com.example.jujuassembly.global.exception;

import com.example.jujuassembly.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<ApiResponse> IllegalArgumentExceptionHandler(IllegalArgumentException ex) {
    ApiResponse apiResponse = new ApiResponse(ex.getMessage(),
        HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse> apiExceptionHandler(ApiException ex) {
    return new ResponseEntity<>(new ApiResponse(ex.getMsg(), ex.getStatus().value()),
        ex.getStatus());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  protected ResponseEntity<ApiResponse> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex) {
    return new ResponseEntity<>(
        new ApiResponse("이미지 크기가 10MB를 초과합니다!", ex.getStatusCode().value()),
        ex.getStatusCode());
  }
}


