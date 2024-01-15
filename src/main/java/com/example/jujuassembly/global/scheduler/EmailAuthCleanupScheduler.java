package com.example.jujuassembly.global.scheduler;

import com.example.jujuassembly.domain.emailAuth.repository.EmailAuthRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmailAuthCleanupScheduler {

  private final EmailAuthRepository emailAuthRepository;

  /**
   * 이메일인증 지간이 지났는데도 완료되지않은 데이터를 삭제하는 스케쥴러
   **/
  @Transactional
  @Scheduled(fixedRate = 5 * 60 * 1000) // 5분에 한번 작동
  public void cleanupEmailAuth() {
    LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(5);
    emailAuthRepository.deleteByCreatedAtBefore(fiveMinAgo);
  }
}
