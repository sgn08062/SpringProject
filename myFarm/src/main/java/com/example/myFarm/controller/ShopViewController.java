package com.example.myFarm.controller;

import org.springframework.stereotype.Controller; // <-- @Controller 사용
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // HTML 뷰 템플릿을 렌더링하는 역할
public class ShopViewController {

    // URL: /shop/view 로 설정 (View를 위한 명확한 경로)
    @GetMapping("/shop/view")
    public String shopView() {
        // Thymeleaf에게 templates.shop/shop.html 파일을 찾아 렌더링하라고 지시
        return "shop/shop";
    }
}