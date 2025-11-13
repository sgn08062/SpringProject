package com.example.myFarm.user.dto;

import lombok.Data;

/**
 * [v12] DB의 'USERS' 테이블과 매핑되는 DTO
 */
@Data
public class MemberDTO {
    // SQL의 INT/BIGINT는 Java에서 Long으로 받는 것이 안전합니다.
    private Long userId;     // from USER_ID
    private String loginId;  // from LOGIN_ID
    private String userPw;   // from USER_PW
    private String userName; // from USER_NAME
    private String phone;    // from PHONE
    private String email;    // from EMAIL
    private String auth;     // from AUTH (ENUM)
}