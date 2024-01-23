package com.example.jujuassembly.domain.notification.dto;

import com.example.jujuassembly.domain.notification.entity.Notification;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationResponseDto {

  private Long id;
  private String content;
  private String url;

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime createdAt;
  private boolean read;


  public NotificationResponseDto(Notification notification) {
    this.id = notification.getId();
    this.content = notification.getContent();
    this.url = notification.getUrl();
    this.createdAt = notification.getCreatedAt();
    this.read = notification.isRead();
  }


}
