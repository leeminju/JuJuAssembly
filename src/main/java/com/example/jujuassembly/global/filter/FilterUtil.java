package com.example.jujuassembly.global.filter;

import com.example.jujuassembly.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilterUtil {

  private final ObjectMapper objectMapper;

  public void setMassageToResponse(String msg, HttpServletResponse response, HttpStatus status) throws IOException {
    ApiResponse apiResponse = new ApiResponse(msg, status.value());
    response.setStatus(status.value());
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }


}
