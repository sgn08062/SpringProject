package com.example.myFarm.account;

import com.example.myFarm.command.UserVO;

public interface AccountService {

    // 신규 유저 회원가입
    int userRegister(UserVO userVO);

    // 유저 로그인
    int userLogin(UserVO userVO);

    // 세션용 유저 아이디 찾기
    int findUserIdForSession(String loginId);

    // 아이디가 존재하는 지 확인
    int isLoginIdExist(String loginId);
}
