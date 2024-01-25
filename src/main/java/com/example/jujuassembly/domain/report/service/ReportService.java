package com.example.jujuassembly.domain.report.service;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.category.repository.CategoryRepository;
import com.example.jujuassembly.domain.notification.repository.NotificationRepository;
import com.example.jujuassembly.domain.notification.service.NotificationService;
import com.example.jujuassembly.domain.report.dto.ReportPatchRequestDto;
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

  private final NotificationService notificationService;


  //생성
  @Transactional
  public ReportResponseDto postReport(Long categoryId, MultipartFile image,
      ReportRequestDto requestDto, User user)
      throws IOException {

    Category category = categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Report report = new Report(requestDto);
    report.updateUser(user);
    report.updateCategory(category);
    reportRepository.save(report);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, S3Manager.REPORT_DIRECTORY_NAME, report.getId());
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
      ReportPatchRequestDto requestDto)
      throws IOException {

    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Report report = reportRepository.getById(reportId);
    if (!report.getCategory().getId().equals(categoryId)) {
      throw new ApiException("현재 카테고리가 아닙니다.", HttpStatus.BAD_REQUEST);
    }

    Category ModifiedCategory = categoryRepository.findCategoryByIdOrElseThrow(requestDto.getModifiedCategoryId());

    report.updateName(requestDto.getName());
    report.updateCategory(ModifiedCategory);

    s3Manager.deleteAllImageFiles(reportId.toString(), S3Manager.REPORT_DIRECTORY_NAME);


    if (image == null || image.getContentType() == null) {
      if (report.getImage() != null) {
          report.updateImage(report.getImage());//원래 이미지 유지
        } else {
       report.updateImage(null);//원래 이미지 삭제
        }
    } else {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.", HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, S3Manager.REPORT_DIRECTORY_NAME, report.getId());
      report.updateImage(imageUrl);
    }
    return new ReportResponseDto(report);
  }

  @Transactional
  public ReportResponseDto patchReportStatus(Long categoryId, Long reportId, ReportStatusRequestDto requestDto, User user) {
    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Report report = reportRepository.getById(reportId);
    report.updateStatus(requestDto.getStatus());

    // 관리자가 상태를 변경했을 때, 제보한 사용자에게 알림 전송
    notificationService.send(report.getUser(), "REPORT", reportId, user);
    return new ReportResponseDto(report);
  }

  //삭제
  @Transactional
  public void deleteReport(Long categoryId, Long reportId) {

    categoryRepository.findCategoryByIdOrElseThrow(categoryId);
    Report report = reportRepository.getById(reportId);

    reportRepository.delete(report);

    s3Manager.deleteAllImageFiles(reportId.toString(), S3Manager.REPORT_DIRECTORY_NAME);
  }


}
