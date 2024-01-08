package com.example.jujuassembly.global.exception;

import com.example.jujuassembly.global.response.ApiResponse;
import java.util.HashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<ApiResponse> IllegalArgumentExceptionHandler(IllegalArgumentException ex) {
    ApiResponse apiResponse = new ApiResponse(ex.getMessage(),
        HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status,
      WebRequest request) {
    BindingResult bindingResult = ex.getBindingResult();
    HashMap<String, String> errors = new HashMap<>();
    bindingResult.getAllErrors()
        .forEach(error -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));

    ApiResponse<HashMap<String, String>> apiResponse = new ApiResponse("입력값이 유효하지 않습니다.",
        HttpStatus.BAD_REQUEST.value(), errors);
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }


  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
      WebRequest request) {
    ApiResponse apiResponse = new ApiResponse<>("누락된 파라미터" + ex.getParameterName(),
        HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse> apiExceptionHandler(ApiException ex) {
    return new ResponseEntity<>(new ApiResponse(ex.getMsg(), ex.getStatus().value()),
        ex.getStatus());
  }
}


