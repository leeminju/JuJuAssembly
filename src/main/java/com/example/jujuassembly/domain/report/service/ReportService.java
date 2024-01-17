package com.example.jujuassembly.domain.report.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.domain.report.dto.ReportResponseDto;
import com.example.jujuassembly.domain.report.dto.ReportStatusRequestDto;
import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.domain.report.repository.ReportRepository;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.exception.ApiException;
import com.example.jujuassembly.global.s3.S3Manager;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final S3Manager s3Manager;
  private final ReportRepository reportRepository;
  private final CategoryRepository categoryRepository;


  //생성
  @Transactional
  public ReportResponseDto postReport(Long categoryId, MultipartFile image,
      ReportRequestDto requestDto, User user)
      throws IOException {

    Category category = categoryRepository.getById(categoryId);
    Report report = new Report(requestDto);
    report.updateUser(user);
    report.updateCategory(category);
    reportRepository.save(report);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, "reports", report.getId());
      report.updateImage(imageUrl);
    }

    return new ReportResponseDto(report);
  }

  //조회
  //전체 제보상품 조회
  public Page<ReportResponseDto> getAllReports(Pageable pageable) {
    Page<Report> allReports = reportRepository.findAll(pageable);
    return allReports.map(ReportResponseDto::new);
  }

  //유저별 제보상품 조회
  public Page<ReportResponseDto> getUserReports(Long userId, Pageable pageable) {
    Page<Report> reports = reportRepository.findAllByUserId(userId, pageable);
    return reports.map(ReportResponseDto::new);
  }

  //카테고리별 제보상품 조회
  public Page<ReportResponseDto> getReportsByCategoryId(Long categoryId, Pageable pageable) {
    Page<Report> reports = reportRepository.findAllByCategoryId(categoryId, pageable);
    return reports.map(ReportResponseDto::new);
  }

  //수정
  @Transactional
  public ReportResponseDto patchReport(Long categoryId, Long reportId, MultipartFile image,
      ReportRequestDto requestDto, User user)
      throws IOException {

    categoryRepository.getById(categoryId);
    Report report = reportRepository.getById(reportId);

    report.updateName(requestDto.getName());

    s3Manager.deleteAllImageFiles(reportId.toString(), "reports");

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, "reports", report.getId());
      report.updateImage(imageUrl);
    }
    return new ReportResponseDto(report);
  }

  //제보상품 상태 변경
  @Transactional
  public ReportResponseDto patchReportStatus(Long categoryId, Long reportId,
      ReportStatusRequestDto requestDto, User user) {

    categoryRepository.getById(categoryId);
    Report report = reportRepository.getById(reportId);

    report.updateStatus(requestDto.getStatus());
    return new ReportResponseDto(report);
  }

  //삭제
  @Transactional
  public void deleteReport(Long categoryId, Long reportId, User user) {

    categoryRepository.getById(categoryId);
    Report report = reportRepository.getById(reportId);

    reportRepository.delete(report);

    s3Manager.deleteAllImageFiles(reportId.toString(), "reports");
  }


}
