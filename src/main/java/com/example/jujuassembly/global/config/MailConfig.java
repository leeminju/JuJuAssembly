package com.example.jujuassembly.global.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

// JavaMailSender에 Bean 주입이 안되고 연결 오류가 생겨서 생성했습니다.
@Configuration
public class MailConfig {

  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);

    // STARTTLS를 명시적으로 추가
    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.smtp.auth", true);
    props.put("mail.smtp.starttls.enable", true);

    mailSender.setUsername(username);
    mailSender.setPassword(password);

    return mailSender;
  }
}
