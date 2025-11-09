package com.example.myFarm.common.security;

import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 해당 클래스가 AOP 기능을 수행한다는 의미 -> 즉, 공통 기능을 실행하는 곳
@Aspect
// Spring에게 해당 클래스를 빈(Bean)으로 등록하도록 지시
@Component
public class LoginCheckAspect {

    @Autowired
    private HttpSession session;


    // 해당 메서드는 접근하려는 사용자에 대해서 세션이 존재하는 지 판단해 세션이 있다면 통과
    // 세션이 존재하지 않으면 접근 차단하는 기능
    // @Before은 메서드 실행 직전에 동작
    // @annotation(LoginCheck) : @LoginCheck가 붙은 메서드만 검사
    @Before("@annotation(com.example.myFarm.common.security.LoginCheck)")
    public void checkLoginSession() {
        Object loginUser = session.getAttribute("loginUser");

        if (loginUser == null) {
            // 해당 기능은 추후에 로그인 화면으로 연결해줘야 함
            System.out.println("접근 차단 : 로그인 세션이 없습니다.");
            throw new RuntimeException("로그인이 필요합니다.");
        }
        else {
            System.out.println("세션 인증 통과 " + loginUser);
        }
    }

}
