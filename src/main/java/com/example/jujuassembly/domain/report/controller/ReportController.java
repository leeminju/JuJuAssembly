package com.example.jujuassembly.domain.report.controller;


import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.domain.report.dto.ReportResponseDto;
import com.example.jujuassembly.domain.report.dto.ReportStatusRequestDto;
import com.example.jujuassembly.domain.report.service.ReportService;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ReportController {

  private final ReportService reportService;

  //제보 상품 생성
  @PostMapping("/categories/{categoryId}/reports")
  public ResponseEntity<ApiResponse> postReport(@PathVariable Long categoryId,
      @RequestParam(value = "image", required = false) MultipartFile image,
      @RequestPart("data") ReportRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails)
      throws IOException {

    ReportResponseDto reportResponseDto = reportService.postReport(categoryId, image, requestDto,
        userDetails.getUser());

    return ResponseEntity.status(HttpStatus.CREATED).body(
        new ApiResponse<>("상품제보 생성 완료", HttpStatus.CREATED.value(), reportResponseDto));
  }

  // user에 해당하는 제보 상품 리스트 조회
  @GetMapping("/users/{userId}/reports")
  public ResponseEntity<ApiResponse> getReports(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    List<ReportResponseDto> reportList = reportService.getReports(userId, userDetails.getUser());

    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 리스트 조회 완료", HttpStatus.OK.value(), reportList));
  }


  // 제보 상품 수정
  @PatchMapping("/cateogries/{categoryId}/reports/{reportId}")
  public ResponseEntity<ApiResponse> patchReport(@PathVariable Long categoryId,
      @PathVariable Long reportId,
      @RequestParam(value = "image", required = false) MultipartFile image,
      @RequestPart("data") ReportRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails)
      throws IOException {

    ReportResponseDto reportResponseDto = reportService.patchReport(categoryId, reportId, image,
        requestDto, userDetails.getUser());

    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 수정 완료", HttpStatus.OK.value(), reportResponseDto));
  }


  //제보 상품 상태 수정
  @Secured(UserRoleEnum.Authority.ADMIN)
  @PatchMapping("/categories/{categoryId}/reports/{reportId}/status")
  public ResponseEntity<ApiResponse> patchReportStatus(@PathVariable Long categoryId,
      @PathVariable Long reportId, @RequestBody ReportStatusRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    ReportResponseDto reportResponseDto = reportService.patchReportStatus(categoryId, reportId,
        requestDto, userDetails.getUser());

    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 수정 완료", HttpStatus.OK.value(), reportResponseDto));
  }

  //제보 상품 삭제
  @DeleteMapping("/categories/{categoryId}/reports/{reportId}")
  public ResponseEntity<ApiResponse> deleteReport(@PathVariable Long categoryId,
      @PathVariable Long reportId, @AuthenticationPrincipal UserDetailsImpl userDetails) {

    reportService.deleteReport(categoryId, reportId, userDetails.getUser());

    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("제보 상품 삭제 성공", HttpStatus.OK.value()));
  }
}
