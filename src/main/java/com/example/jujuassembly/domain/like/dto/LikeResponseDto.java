package com.example.jujuassembly.domain.like.dto;

import com.example.jujuassembly.domain.like.entity.Like;
import com.example.jujuassembly.domain.product.entity.Product;
import com.example.jujuassembly.domain.user.entity.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.Hibernate;


@Getter
public class LikeResponseDto {

  private Long id;
  private String product;
  private String user;

  public LikeResponseDto(Like like) {
    this.id = like.getId();
    this.product = like.getProduct().getName();
    this.user = like.getUser().getNickname();
  }
}
