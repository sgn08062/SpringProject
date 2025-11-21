package com.example.myFarm.user;
import com.example.myFarm.command.UserVO;
import com.example.myFarm.command.ShopVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;


    @Override
    public String getUserName(int userId) {
        return userMapper.selectUserName(userId);
    }

    @Override
    public UserVO getUserInfo(int userId) {
        return userMapper.selectUserInfo(userId);
    }

    @Override
    public ShopVO getItemDetail(Long itemId) {
        return userMapper.selectItemDetail(itemId);
    }

    @Override
    public List<ShopVO> getShopItemList(String searchKeyword) { // ⭐️ 파라미터 수정
        return userMapper.selectShopItemList(searchKeyword);
    }
}