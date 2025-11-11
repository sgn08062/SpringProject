package com.example.myFarm.account;

import com.example.myFarm.command.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    int userRegisterDB(UserVO userVO); // 신규 유저 회원가입

    int userLoginDB(UserVO userVO); // 유저 로그인
    
    int findUserIdForSessionDB(@Param("loginId") String loginId); // 세션에 저장할 사용자 ID값 찾기

    int isLoginIdExistDB(String loginId); // 아이디가 존재하는 지 확인

}
