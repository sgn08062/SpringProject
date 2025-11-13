package com.example.myFarm.user.dto;

import lombok.Data;

/**
 * [v12] script.js의 회원가입 폼(signupData)이 보내는 JSON을 받기 위한 DTO
 */
@Data
public class SignupRequestDTO {
    private String username; // script.js가 보낸 ID (-> loginId)
    private String password; // script.js가 보낸 PW (-> userPw)
    private String name;     // script.js가 보낸 이름 (-> userName)
    private String phone;    // script.js가 보낸 폰
    private String address;  // script.js가 보낸 주소 (-> ADDRESS 테이블)
}