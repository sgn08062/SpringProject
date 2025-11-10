package com.example.myFarm.controller;

import com.example.myFarm.account.AccountService;
import com.example.myFarm.command.UserVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // 회원가입 요청
    @PostMapping(value = "/register", consumes = "application/x-www-form-urlencoded;charset=UTF-8", produces = "text/plain")
    public String register(UserVO uservo){
        System.out.println(uservo.toString());
        int result = accountService.userRegister((uservo));
        if (result == 1) {
            return "success";
        }
        else {
            return "fail";
        }
    }

    // 로그인 요청
    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded;charset=UTF-8", produces = "text/plain")
    public String login(UserVO uservo, HttpSession session){
        System.out.println(uservo.toString());
        int result = accountService.userLogin((uservo));
        if (result == 1) {
            int userId = accountService.findUserIdForSession(uservo.getLoginId());
            session.setAttribute("userId", userId); // 세션에 오직 USER_ID만 저장
            System.out.println("로그인 세션 생성 완료 : " + session.getId() + ", userId=" + userId);
            return "success";
        }
        else {
            return "fail";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        System.out.println("로그아웃 요청 들어옴. Session ID : " + session.getId());
        session.invalidate();  // 해당 세션 삭제
        System.out.println("세션 삭제 완료 : " + session.getId());
        return "logout success";
    }
}
