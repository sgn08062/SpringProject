package com.example.myFarm.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // 1. 아까 추가한 주문 결제 페이지 매핑
    @GetMapping("/checkout")
    public String checkoutPage() {
        return "checkout"; // templates/checkout.html
    }

    // 2. ⭐️이 부분을 새로 추가해 주세요!⭐️
    //   메인 페이지("/") URL 요청을 받으면
    //   templates/index.html 파일을 보여줍니다.
    @GetMapping("/")
    public String mainPage() {
        return "index"; // "templates/index.html"을 의미
    }
}