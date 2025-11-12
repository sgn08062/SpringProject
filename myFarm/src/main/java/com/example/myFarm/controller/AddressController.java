package com.example.myFarm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 페이지 관련 컨트롤러
@Controller
@RequestMapping("/user/address")
public class AddressController {

    // 주소 조회 페이지
    @GetMapping("/")
    public String getAddressPage() {
        return "address/list";
    }
}
