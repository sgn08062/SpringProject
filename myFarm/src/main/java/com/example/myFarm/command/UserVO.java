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
    private String userPw; //USER_PW
    private String userName; //USER_NAME
    private String userPhone; //USER_PHONE
    private String userEmail; //USER_EMAIL
    private String auth; //AUTH
}
