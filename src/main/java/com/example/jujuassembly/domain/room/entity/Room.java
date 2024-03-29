package com.example.jujuassembly.domain.room.entity;

import com.example.jujuassembly.domain.chat.entity.Chat;
import com.example.jujuassembly.domain.user.entity.User;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "rooms")
public class Room extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; //채팅방 id

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private User admin; //채팅방은 관리자와 유저가 대화한다고 가정하고 관리자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private User user;//대화할때 관리자가 아닌 유저

  public Room(User admin, User user) {
    this.admin = admin;
    this.user = user;
  }

  public User getPartner(Long id) {
    if (user.hasSameId(id)) {
      return admin;
    }
    return user;
  }
}