package com.example.jujuassembly.domain.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ProductViewController {

  @GetMapping("/categories/{categoryId}/products")
  public String productPage(){
    return "product";
  }


}
