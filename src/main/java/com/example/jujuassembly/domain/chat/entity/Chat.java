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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chats")
public class Chat extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;//chat 아이디

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Room room;//채팅방

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private User sender;//chat을 보낸 사람

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private User receiver;//chat을 받는 사람

  @Lob
  @Column(nullable = false)
  private String content;//채팅 내용

  public Chat(Room room, User sender, User receiver, String content) {
    this.room = room;
    this.sender = sender;
    this.receiver = receiver;
    this.content = content;
  }
}