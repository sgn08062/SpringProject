package com.example.myFarm.util.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    // AJAX 요청 판단 메서드
    private boolean isAjax(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                || (accept != null && accept.contains("application/json"));
    }

    // 비로그인 처리 메서드 -> 비로그인 시 로그인 페이지로 리다이렉트
    private void handleUnauthenticated(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (isAjax(req)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("""
                {"code":"UNAUTHENTICATED","message":"로그인이 필요합니다.","loginUrl":"/account/login"}
            """);
        } else {
            res.sendRedirect("/account/login");
        }
    }

    // 권한 없음(Forbidden) 처리 메서드
    private void handleForbidden(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (isAjax(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("""
                {"code":"FORBIDDEN","message":"접근 권한이 없습니다."}
            """);
        } else {
            // 원하는 페이지로 보내면 됨 (403 페이지 or 메인 등)
            res.sendRedirect("/error/403");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        // 정적 리소스 등은 통과
        if (!(handler instanceof HandlerMethod)) return true;

        String uri = req.getRequestURI();

        HttpSession session = req.getSession(false);
        Object userId = (session == null) ? null : session.getAttribute("userId");
        String auth = (session == null) ? null : (String) session.getAttribute("auth"); 
        // "USER" / "ADMIN" 에 따라 분기 나뉨 -> USER에서 CONSUMER로 변경 필요

        // 1) 로그인 안 된 경우: 기존 로직 그대로
        if (userId == null) {
            handleUnauthenticated(req, res);
            System.out.println("비로그인 접근 차단: " + uri);
            return false;
        }

        // 2) 권한 체크 (로그인은 되어 있음)
        boolean isAdminPage = uri.startsWith("/admin/");
        boolean isUserPage  = uri.startsWith("/user/");

        // USER 권한은 /admin/** 금지
        if (isAdminPage && !"ADMIN".equals(auth)) {
            handleForbidden(req, res);
            System.out.println("USER가 ADMIN 페이지 접근 시도: " + uri);
            return false;
        }

        // ADMIN 권한은 /user/** 금지 (요구사항 그대로 반영)
        if (isUserPage && !"CUSTOMER".equals(auth)) {
            handleForbidden(req, res);
            System.out.println("ADMIN이 USER 페이지 접근 시도: " + uri);
            return false;
        }

        // 그 외는 통과
        return true;
    }

//    @Override
//    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
//        // 정적 리소스 등은 통과
//        if (!(handler instanceof HandlerMethod)) return true;
//
//        // 세션에서 로그인 여부 확인 (userId 기준)
//        HttpSession session = req.getSession(false);
//        Object userId = (session == null) ? null : session.getAttribute("userId");
//        if (userId != null) return true; // 로그인 O2K
//
//        // AJAX/JSON이면 401, 그 외엔 로그인 화면으로 리다이렉트
//        String accept = req.getHeader("Accept");
//        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
//                || (accept != null && accept.contains("application/json"));
//
//        if (isAjax) {
//            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            res.setContentType("application/json;charset=UTF-8");
//            res.getWriter().write("""
//                  {"code":"UNAUTHENTICATED","message":"로그인이 필요합니다.","loginUrl":"/login"}
//            """);
//        } else {
//            res.sendRedirect("/account/login");
//        }
//        System.out.println("접근차단");
//        return false; // 컨트롤러 진입 차단
//    }

}
