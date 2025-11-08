package com.example.myFarm.account;

import com.example.myFarm.command.UserVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper {
    int userRegisterDB(UserVO userVO); // 신규 유저 회원가입

    int userLoginDB(UserVO userVO); // 유저 로그인
}
