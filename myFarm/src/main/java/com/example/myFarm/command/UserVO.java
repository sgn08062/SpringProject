package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO {
    private int userId; //USER_ID
    private String loginId; //LOGIN_ID
    private String userPw; //USER_PW
    private String userName; //USER_NAME
    private String phone; //PHONE
    private String email; //EMAIL
    private String auth; //AUTH
}
