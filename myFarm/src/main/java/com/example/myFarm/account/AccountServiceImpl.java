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

    @Override
    public int userRegister(UserVO userVO) {
        return accountMapper.userRegisterDB(userVO);
    }
}
