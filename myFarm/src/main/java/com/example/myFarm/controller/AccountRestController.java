package com.example.myFarm.controller;

import com.example.myFarm.account.AccountService;
import com.example.myFarm.command.UserVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    @Autowired
    private AccountService accountService;

    // 회원가입 기능 API
    @Transactional(rollbackFor = Exception.class)
    @PostMapping(
            value = "/register",
            consumes = "application/x-www-form-urlencoded;charset=UTF-8",
            produces = "text/plain"
    )
    public String register(UserVO uservo){

        System.out.println(uservo.toString());

        int result = accountService.userRegister((uservo));

        if (result == 1) {
            System.out.println("회원가입 성공");
            return "success";
        }
        else {
            System.out.println("회원가입 실패");
            return "fail";
        }
    }

    // 로그인 기능  API
    @PostMapping(
            value = "/login",
            consumes = "application/x-www-form-urlencoded;charset=UTF-8",
            produces = "text/plain"
    )
    public String login(UserVO uservo, HttpSession session){

        System.out.println(uservo.toString());

        int result = accountService.userLogin((uservo));

        if (result == 1) {
            int userId = accountService.findUserIdForSession(uservo.getLoginId());
            String auth = accountService.findAuthForSession(uservo.getLoginId());
            session.setAttribute("userId", userId); // 세션에 USER_ID 저장
            session.setAttribute("auth", auth); // 세션에 AUTH 저장
            System.out.println("로그인 세션 생성 완료 : " + session.getId() + ", userId=" + userId);
            return "success";
        }
        else {
            return "fail";
        }
    }

    // 로그아웃 기능 API
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        String sid = session.getId();
        System.out.println("로그아웃 요청 들어옴. Session ID : " + sid);
        session.invalidate();  // 해당 세션 삭제
        System.out.println("세션 삭제 완료 : " + sid);
        return "success";
    }

    // 아이디 중복 확인 기능 API
    @PostMapping(
            value = "/idcheck",
            consumes = "application/x-www-form-urlencoded;charset=UTF-8",
            produces = "text/plain"
    )
    public String idcheck(@RequestParam("loginId") String loginId){

        int result = accountService.isLoginIdExist(loginId);

        if (result >= 1) {
            return "dup";
        }
        else {
            return "unique";
        }
    }
}
