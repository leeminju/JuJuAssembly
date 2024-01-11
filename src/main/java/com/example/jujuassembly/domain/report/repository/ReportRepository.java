package com.example.jujuassembly.domain.report.repository;

import com.example.jujuassembly.domain.report.entity.Report;
import com.example.jujuassembly.global.exception.ApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public interface ReportRepository extends JpaRepository<Report, Long> {

  default Report getById(Long id) {
    return findById(id).orElseThrow(
        () -> new ApiException("해당하는 상품 제보가 없습니다.", HttpStatus.NOT_FOUND)
    );
  }

  List<Report> findAllByUserId(Long userId);

  Optional<Report> findByUserId(Long userId);
}
