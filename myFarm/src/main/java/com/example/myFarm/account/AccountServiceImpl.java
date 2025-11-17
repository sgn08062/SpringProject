package com.example.myFarm.account;

import com.example.myFarm.command.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    @Qualifier("accountMapper")
    private AccountMapper accountMapper;

    // 유저 회원가입 서비스
    @Override
    public int userRegister(UserVO userVO) {
        return accountMapper.userRegisterDB(userVO);
    }

    // 유저 로그인 서비스
    @Override
    public int userLogin(UserVO userVO) {
        return accountMapper.userLoginDB(userVO);
    }

    // 세션 저장용 유저ID 조회
    @Override
    public int findUserIdForSession(String userId) { return accountMapper.findUserIdForSessionDB(userId); }

    // 아이디 중복 체크
    @Override
    public int isLoginIdExist(String loginId) {
        return accountMapper.isLoginIdExistDB(loginId);
    }
}
