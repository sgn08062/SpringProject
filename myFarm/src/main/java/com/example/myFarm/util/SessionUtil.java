/*
package com.example.myFarm.util; // util 패키지에 위치

import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    public static final String USER_ID_SESSION_KEY = "userId";

    public static int getCurrentUserId(HttpSession session) {
        Object userIdObject = session.getAttribute(USER_ID_SESSION_KEY);

        if (userIdObject != null) {
            try {
                return (Integer) userIdObject;
            } catch (ClassCastException e) {
                System.err.println("세션에 저장된 사용자 ID가 정수형이 아닙니다: " + userIdObject.getClass().getName());
                return 1;
            }
        }
        return 1;
    }
}*/
