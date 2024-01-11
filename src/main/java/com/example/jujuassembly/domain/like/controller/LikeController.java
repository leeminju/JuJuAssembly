package com.example.jujuassembly.domain.like.controller;

import com.example.jujuassembly.domain.like.dto.LikeResponseDto;
import com.example.jujuassembly.domain.like.service.LikeService;
import com.example.jujuassembly.global.response.ApiResponse;
import com.example.jujuassembly.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class LikeController {

  private final LikeService likeService;

  /**
   * 제품에 대한 '좋아요' 추가
   *
   * @param categoryId  좋아요를 추가할 제품이 속한 카테고리
   * @param productId   좋아요를 추가할 제품의 Id
   * @param userDetails 좋아요를 수행할 인증된 사용자
   * @return 작업 성공 여부를 나타내는 ApiResponse가 포함된 ResponseEntity
   */

  @PostMapping("/categories/{categoryId}/products/{productId}/like")
  private ResponseEntity<ApiResponse> addLike(
      @PathVariable(value = "categoryId") Long categoryId,
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    likeService.addLike(categoryId, productId, userDetails.getUser());

    return ResponseEntity.ok()
        .body(new ApiResponse<>("좋아요", HttpStatus.OK.value()));
  }

  /**
   * 사용자별 좋아요 목록
   *
   * @param userId           좋아요 목록을 조회할 사용자의 아이디
   * @param loginUserDetails 좋아요 목록을 조회할 로그인한 사용자
   * @return 작업 성공 여부와 좋아요 리스트를 담은 ResponseEntity
   */
  @GetMapping("/user/{userId}/like")
  private ResponseEntity<ApiResponse<List<LikeResponseDto>>> viewLikeProducts(
      @PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl loginUserDetails) {
    List<LikeResponseDto> likeList = likeService.viewLikeProducts(userId,
        loginUserDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse<>("좋아요 목록 조회", HttpStatus.OK.value(), likeList));
  }


  /**
   * 좋아요 취소
   *
   * @param productId   좋아요 취소를 할 상품
   * @param userDetails 좋아요 취소를 수행할 로그인 한 사용자 정보
   * @return 좋아요 취소 성공 여부와 좋아요 리스트를 담은 ResponseEntity
   */
  //좋아요 취소
  @DeleteMapping("/categories/{categoryId}/products/{productId}/like")
  private ResponseEntity<ApiResponse<List<LikeResponseDto>>> cancelLike(
      @PathVariable Long productId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    List<LikeResponseDto> likeResponseDto = likeService.cancelLike(productId,
        userDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse<>("좋아요", HttpStatus.OK.value(), likeResponseDto));
  }


}
