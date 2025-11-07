package com.example.myFarm.controller;

import com.example.myFarm.account.AccountService;
import com.example.myFarm.command.UserVO;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // 회원가입 요청
    @PostMapping(value = "/register", consumes = "application/x-www-form-urlencoded;charset=UTF-8", produces = "text/plain")
    public String register(@RequestParam Map<String, Object> map){
        System.out.println(map.toString());
        return "success";
    }

}
