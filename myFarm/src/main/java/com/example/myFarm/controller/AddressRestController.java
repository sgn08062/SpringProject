package com.example.myFarm.controller;

import com.example.myFarm.address.AddressService;
import com.example.myFarm.command.AddressVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails.Address;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 페이지 API 관련 컨트롤러
@RestController
@RequestMapping("/user/api/address")
public class AddressRestController {

    @Autowired
    private AddressService addressService;

    // 주소  조회 API
    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity<List<AddressVO>> insertAddress(HttpSession session) {
        Object uid = session.getAttribute("userId");

        List<AddressVO> list = addressService.getAddressList((Integer) uid);

        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/insert", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> insertAddress(@RequestBody AddressVO addressVO,
                                HttpSession session) {

        Object uid = session.getAttribute("userId");
        addressVO.setUserId((Integer) uid);
        int result = addressService.addressInsert(addressVO);

        if (result == 1) {
            return ResponseEntity.ok("success");
        }
        else {
            return ResponseEntity.status(500).body("fail");
        }
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
