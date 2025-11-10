package com.example.myFarm.account;

import com.example.myFarm.command.UserVO;

public interface AccountService {
    int userRegister(UserVO userVO); // 신규 유저 회원가입
    
    int userLogin(UserVO userVO); // 유저 로그인

    int findUserIdForSession(String loginId);
}
