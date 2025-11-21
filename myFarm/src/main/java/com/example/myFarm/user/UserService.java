package com.example.myFarm.user;

import com.example.myFarm.command.ShopVO;
import com.example.myFarm.command.UserVO;
import java.util.List;

public interface UserService {

    // 사용자 이름 조회
    String getUserName(int userId);
    UserVO getUserInfo(int userId);

    // 아이템 이름 조회
    ShopVO getItemDetail(Long itemId);
    List<ShopVO> getShopItemList(String searchKeyword);


}