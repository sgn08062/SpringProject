package com.example.myFarm.controller;

import com.example.myFarm.address.AddressService;
import com.example.myFarm.command.AddressVO;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 페이지 API 관련 컨트롤러
@RestController
@RequestMapping({"/user/api/address", "/admin/api/address"})
public class AddressRestController {

    @Autowired
    private AddressService addressService;

    // 주소  조회 API
    @GetMapping(
            value = "/",
            produces = "application/json"
    )
    public ResponseEntity<List<AddressVO>> getAddressList(HttpSession session) {
        Object uid = session.getAttribute("userId");

        List<AddressVO> list = addressService.getAddressList((Integer) uid);
        System.out.println("농가 아이디: "+ list.get(0).getAddressId());

        return ResponseEntity.ok(list);
    }

    // 주소 추가 API
    @PostMapping(
            value = "/insert",
            consumes = "application/json",
            produces = "text/plain"
    )
    public ResponseEntity<String> insertAddress(
            @RequestBody AddressVO addressVO,
            HttpSession session
    )
    {
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
    @PostMapping(
            value = "/update",
            consumes = "application/json",
            produces = "text/plain"
    )
    public ResponseEntity<String> updateAddress(
            @RequestBody AddressVO addressVO,
            HttpSession session
    ) {
        Object uid = session.getAttribute("userId");

        addressVO.setUserId((Integer) uid);

        int result = addressService.addressUpdate(addressVO);

        if (result == 1) {
            return ResponseEntity.ok("success");
        }
        else {
            return ResponseEntity.status(500).body("fail");
        }
    }

    // 주소 삭제 API
    @PostMapping("/delete")
    public ResponseEntity<String> deleteAddress(
            @RequestBody AddressVO addressVO,
            HttpSession session
    ) {

        int addressId = addressVO.getAddressId();

        Object uid = session.getAttribute("userId");

        int result = addressService.addressDelete(addressId,(Integer)uid);

        if (result == 1) {
            return ResponseEntity.ok("success");
        }
        else {
            return ResponseEntity.status(500).body("fail");
        }
    }
}
