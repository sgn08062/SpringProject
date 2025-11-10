package com.example.myFarm.controller;

import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails.Address;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 페이지 API 관련 컨트롤러
@RestController
@RequestMapping("/user/address")
public class AddressApiController {

    // 주소 추가 API
    @PostMapping("/insert")
    public String insertAddress(@RequestBody Address address) {
        return "not yet implemented";
    }

    // 주소 수정 API
    @PostMapping("/update")
    public String updateAddress(@RequestBody Address address) {
        return "not yet implemented";
    }

    // 주소 삭제 API
    @PostMapping("/delete")
    public String deleteAddress(@RequestBody Address address) {
        return "not yet implemented";
    }
}
