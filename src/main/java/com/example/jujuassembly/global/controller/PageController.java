package com.example.jujuassembly.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

  @GetMapping("/")
  public String home() {
    return "index";
  }

  @GetMapping("/mypage")
  public String myPage() {
    return "mypage";
  }

  @GetMapping("/userReview")
  public String userReviewPage() {
    return "userReview";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/signup")
  public String signupPage() {
    return "signup";
  }

  @GetMapping("/admin")
  public String adminPage() {
    return "admin";
  }

  @GetMapping("/admin/category")
  public String categoryPage() {
    return "category";
  }

  @GetMapping("/admin/chat")
  public String chatPage() {
    return "chat";
  }

  @GetMapping("/admin/product")
  public String productPage() {
    return "product";
  }

  @GetMapping("/admin/report")
  public String reportManagementPage() {
    return "reportManagement";
  }

  @GetMapping("/main/myLike")
  public String myLikePage() {
    return "myLike";
  }

  @GetMapping("/main/myReview")
  public String myreviewPage() {
    return "myReview";
  }

  @GetMapping("/main/product")
  public String mainProductPage() {
    return "mainProduct";
  }

  @GetMapping("/main/report")
  public String reportPage() {
    return "report";
  }

  @GetMapping("/main/search")
  public String searchPage() {
    return "search";
  }

  @GetMapping("/main/chat")
  public String userChat() {
    return "userChat";
  }

  //카테고리 별 상품 보기
  @GetMapping("/admin/categories/{categoryId}/products")
  public String productByCategory() {
    return "productByCategory";
  }

  //전체 상품보기
  @GetMapping("/products")
  public String viewAllProducts() {
    return "allProducts";
  }

  @GetMapping("/productDetails")
  public String productDetailsPage() {
    return "productDetails";
  }
}
