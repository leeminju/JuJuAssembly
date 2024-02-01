package com.example.jujuassembly.domain.chat.entity;


import com.example.jujuassembly.domain.room.entity.Room;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chats")
public class Chat {

  @Id
  private ObjectId id;//chat 아이디

  private Long roomId;//채팅방

  private Long senderId;//chat을 보낸 사람

  private Long receiverId;//chat을 받는 사람

  private String content;//채팅 내용

  private LocalDateTime createdAt;

  private String senderImage;

  private String senderNickname;

  public void updateSender(String senderImage,String senderNickname){
    this.senderImage = senderImage;
    this.senderNickname = senderNickname;
  }

}