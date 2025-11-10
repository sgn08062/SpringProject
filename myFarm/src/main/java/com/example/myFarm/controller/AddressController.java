package com.example.myFarm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// 페이지 관련 컨트롤러
@Controller
@RequestMapping("/user/address")
public class AddressController {

    // 주소 조회 페이지 (추후 수정 필요)
    @GetMapping("/")
    public void getAddressPage() {

    }

    // 주소 추가 페이지 (추후 수정 필요)
    @GetMapping("/insert")
    public void getAddressInsertPage() {
    }
    @GetMapping("/update")
    public void getAddressUpdatePage() {}

    //  주소 수정 페이지 (추후 수정 필요)
    @GetMapping("/update")
    public void getAddressUpdatePage(@PathVariable String id) {

    }
}
