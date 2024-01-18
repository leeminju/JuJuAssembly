package com.example.jujuassembly.domain.report.controller;

import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.domain.report.dto.ReportResponseDto;
import com.example.jujuassembly.domain.report.dto.ReportStatusRequestDto;
import com.example.jujuassembly.domain.report.service.ReportService;
import com.example.jujuassembly.domain.user.entity.UserRoleEnum.Authority;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

  /**
   * 제보 상품을 생성합니다.
   *
   * @param categoryId  카테고리 ID
   * @param image       이미지 파일
   * @param requestDto  제보 요청 DTO
   * @param userDetails 인증된 사용자 정보
   * @return ResponseEntity<ApiResponse> 객체
   * @throws IOException 이미지 파일 처리 중 발생하는 예외
   */
  @PostMapping("/categories/{categoryId}/reports")
  public ResponseEntity<ApiResponse> postReport(@PathVariable Long categoryId,
      @RequestParam MultipartFile image,
      @Valid @RequestPart("data") ReportRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails)
      throws IOException {

    ReportResponseDto reportResponseDto = reportService.postReport(categoryId, image, requestDto,
        userDetails.getUser());

    return ResponseEntity.status(HttpStatus.CREATED).body(
        new ApiResponse<>("상품제보 생성 완료", HttpStatus.CREATED.value(), reportResponseDto));
  }

  /**
   * 모든 제보 상품 리스트를 조회합니다.(관리자)
   *
   * @param pageable 페이지네이션 정보 (기본값: 페이지 크기 10, 생성일 기준 내림차순 정렬)
   * @return ResponseEntity<ApiResponse> 객체
   */

  @Secured(Authority.ADMIN)
  @GetMapping("/reports")
  public ResponseEntity<ApiResponse> getAllReports(
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReportResponseDto> allReports = reportService.getAllReports(pageable);
    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 전체 조회 완료", HttpStatus.OK.value(), allReports));
  }

  /**
   * 특정 사용자의 제보 상품 리스트를 조회합니다.(유저,관리자)
   *
   * @param userId   사용자 ID
   * @param pageable 페이지네이션 정보 (기본값: 페이지 크기 10, 생성일 기준 내림차순 정렬)
   * @return ResponseEntity<ApiResponse> 객체
   */
  @GetMapping("/users/{userId}/reports")
  public ResponseEntity<ApiResponse> getUserReports(@PathVariable Long userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReportResponseDto> reports = reportService.getUserReports(userId, pageable);
    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 유저별 조회 완료", HttpStatus.OK.value(), reports));
  }

  /**
   * 특정 카테고리별 제보 상품 리스트를 조회합니다.(관리자)
   *
   * @param categoryId 카테고리 ID
   * @param pageable   페이지네이션 정보 (기본값: 페이지 크기 10, 생성일 기준 내림차순 정렬)
   * @return ResponseEntity<ApiResponse> 객체
   */
  @Secured(Authority.ADMIN)
  @GetMapping("/categories/{categoryId}/reports")
  public ResponseEntity<ApiResponse> getReportsByCategoryId(@PathVariable Long categoryId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<ReportResponseDto> reports = reportService.getReportsByCategoryId(categoryId, pageable);
    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 카테고리별 조회 완료", HttpStatus.OK.value(), reports));
  }

  /**
   * 제보 상품을 수정합니다.(유저,관리자)
   *
   * @param categoryId  카테고리 ID
   * @param reportId    제보 상품 ID
   * @param image       이미지 파일
   * @param requestDto  제보 요청 DTO
   * @param userDetails 인증된 사용자 정보
   * @return ResponseEntity<ApiResponse> 객체
   * @throws IOException 이미지 파일 처리 중 발생하는 예외
   */
  @PatchMapping("/cateogries/{categoryId}/reports/{reportId}")
  public ResponseEntity<ApiResponse> patchReport(@PathVariable Long categoryId,
      @PathVariable Long reportId,
      @RequestParam MultipartFile image,
      @Valid @RequestPart("data") ReportRequestDto requestDto)
      throws IOException {

    ReportResponseDto reportResponseDto = reportService.patchReport(categoryId, reportId, image,
        requestDto);

    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 수정 완료", HttpStatus.OK.value(), reportResponseDto));
  }

  /**
   * 제보 상품의 상태를 수정합니다. (관리자 권한 필요)
   *
   * @param categoryId 카테고리 ID
   * @param reportId   제보 상품 ID
   * @param requestDto 제보 상태 요청 DTO
   * @return ResponseEntity<ApiResponse> 객체
   */
  @Secured(Authority.ADMIN)
  @PatchMapping("/categories/{categoryId}/reports/{reportId}/status")
  public ResponseEntity<ApiResponse> patchReportStatus(@PathVariable Long categoryId,
      @PathVariable Long reportId, @Valid @RequestBody ReportStatusRequestDto requestDto) {

    ReportResponseDto reportResponseDto = reportService.patchReportStatus(categoryId, reportId,
        requestDto);

    return ResponseEntity.status(HttpStatus.OK).body(
        new ApiResponse<>("상품 제보 수정 완료", HttpStatus.OK.value(), reportResponseDto));
  }

  /**
   * 제보 상품을 삭제합니다.(유저,관리자)
   *
   * @param categoryId 카테고리 ID
   * @param reportId   제보 상품 ID
   * @return ResponseEntity<ApiResponse> 객체
   */
  @DeleteMapping("/categories/{categoryId}/reports/{reportId}")
  public ResponseEntity<ApiResponse> deleteReport(@PathVariable Long categoryId,
      @PathVariable Long reportId) {

    reportService.deleteReport(categoryId, reportId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(new ApiResponse("제보 상품 삭제 성공", HttpStatus.OK.value()));
  }
}
