package com.example.myFarm.util.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        // 정적 리소스 등은 통과
        if (!(handler instanceof HandlerMethod)) return true;

        // 세션에서 로그인 여부 확인 (userId 기준)
        HttpSession session = req.getSession(false);
        Object userId = (session == null) ? null : session.getAttribute("userId");
        if (userId != null) return true; // 로그인 OK

        // AJAX/JSON이면 401, 그 외엔 로그인 화면으로 리다이렉트
        String accept = req.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                || (accept != null && accept.contains("application/json"));

        if (isAjax) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("""
                  {"code":"UNAUTHENTICATED","message":"로그인이 필요합니다.","loginUrl":"/login"}
            """);
        } else {
            res.sendRedirect("/account/login");
        }
        System.out.println("접근차단");
        return false; // 컨트롤러 진입 차단
    }

}
