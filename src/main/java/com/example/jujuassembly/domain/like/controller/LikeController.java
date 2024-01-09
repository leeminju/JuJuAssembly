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


  //좋아요
  @PostMapping("/categories/{categoryId}/products/{productId}/like")
  private ResponseEntity<ApiResponse> addLike(
      @PathVariable (value = "categoryId") Long categoryId,
      @PathVariable (value = "productId") Long productId,
      @AuthenticationPrincipal UserDetailsImpl userDetails){

    likeService.addLike(categoryId,productId,userDetails.getUser());

    return ResponseEntity.ok()
        .body(new ApiResponse<>("좋아요",HttpStatus.OK.value()));
 }

 //사용자별 좋아요 목록
  @GetMapping ("/user/{userId}/like")
  private ResponseEntity<ApiResponse<List<LikeResponseDto>>> viewLikeProducts(@PathVariable Long userId,
      @AuthenticationPrincipal UserDetailsImpl loginUserDetails){
    List<LikeResponseDto> likeList = likeService.viewLikeProducts(userId,loginUserDetails.getUser());
    return ResponseEntity.ok()
        .body(new ApiResponse<>("좋아요 목록 조회", HttpStatus.OK.value(),likeList));
  }


  //좋아요 취소
   @DeleteMapping("/categories/{categoryId}/products/{productId}/like")
   private ResponseEntity<ApiResponse<List<LikeResponseDto>>> cancelLike(@PathVariable Long productId,
       @AuthenticationPrincipal UserDetailsImpl userDetails){
     List<LikeResponseDto> likeResponseDto = likeService.cancelLike(productId,userDetails.getUser());
     return ResponseEntity.ok()
         .body(new ApiResponse<>("좋아요",HttpStatus.OK.value(),likeResponseDto));
   }


}
