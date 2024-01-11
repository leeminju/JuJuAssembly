package com.example.jujuassembly.domain.report.repository;

import com.example.jujuassembly.domain.report.entity.Report;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,Long> {


  List<Report> findAllByUserId(Long userId);

  Optional<Report> findByUserId(Long userId);
}
