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
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ApiException("카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    Report report = new Report(requestDto);
    report.updateUser(user);
    report.updateCategory(category);
    reportRepository.save(report);

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.",HttpStatus.BAD_REQUEST);
      }
      String imageUrl = s3Manager.upload(image, "reports", report.getId());
      report.updateImage(imageUrl);
    }

    return new ReportResponseDto(report);
  }

  //조회

  public List<ReportResponseDto> getReports(Long userId, User user) {

    if (!user.getId().equals(userId)) {
      throw new ApiException("제보자가 아닙니다.",HttpStatus.FORBIDDEN);
    }
    reportRepository.existsReportByUserId(user.getId())
        .orElseThrow(() -> new ApiException("해당하는 제보가 없습니다.", HttpStatus.NOT_FOUND));
    List<Report> reportList = reportRepository.findAllByUserId(user.getId());
    return reportList.stream().map(ReportResponseDto::new).toList();
  }

  //수정
  @Transactional
  public ReportResponseDto patchReport(Long categoryId, Long reportId, MultipartFile image,
      ReportRequestDto requestDto, User user)
      throws IOException {

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ApiException("해당하는 카테고리가 없습니다.", HttpStatus.NOT_FOUND));
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new ApiException("해당하는 상품 제보가 없습니다.", HttpStatus.NOT_FOUND));

    if (!report.getUser().getId().equals(user.getId())) {
      throw new ApiException("권한이 없습니다.", HttpStatus.UNAUTHORIZED);
    }

    report.updateName(requestDto.getName());

    s3Manager.deleteAllImageFiles(reportId.toString(), "reports");

    if (image != null && !image.isEmpty()) {
      if (!image.getContentType().startsWith("image")) {
        throw new ApiException("이미지 파일 형식이 아닙니다.",HttpStatus.BAD_REQUEST);
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

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ApiException("해당하는 카테고리가 없습니다.", HttpStatus.NOT_FOUND));
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new ApiException("해당하는 상품 제보가 없습니다.", HttpStatus.NOT_FOUND));

    report.updateStatus(requestDto.getStatus());
    return new ReportResponseDto(report);
  }

  //삭제
  @Transactional
  public void deleteReport(Long categoryId, Long reportId, User user) {

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ApiException("해당하는 카테고리가 없습니다.", HttpStatus.NOT_FOUND));
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new ApiException("해당하는 상품 제보가 없습니다.", HttpStatus.NOT_FOUND));
    if (!report.getUser().getId().equals(user.getId())) {
      throw new ApiException("권한이 없습니다.", HttpStatus.UNAUTHORIZED);

    } reportRepository.delete(report);

    s3Manager.deleteAllImageFiles(reportId.toString(), "reports");
  }


}