package com.example.jujuassembly.domain.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomRequestDto {

  /**
   * 이용자 id
   */
  @NotNull
  private Long userId;
  /**
   * 관리자 id
   */
  @NotNull
  private Long adminId;

}
