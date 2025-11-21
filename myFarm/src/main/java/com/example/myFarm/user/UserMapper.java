package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.UserVO;
import com.example.myFarm.command.ShopVO; // ShopVO 추가
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {

    // USER_NAME을 조회하는 메서드
    String selectUserName(@Param("userId") int userId);

    // USER_INFO를 조회하는 메서드
    UserVO selectUserInfo(int userId);

    // --- 아이템 조회 추가 ---
    ShopVO selectItemDetail(@Param("itemId") Long itemId);
    List<ShopVO> selectShopItemList(@Param("searchKeyword") String searchKeyword);

    List<AddressVO> selectAddressList(@Param("userId") int userId);
    AddressVO selectDefaultAddress(@Param("userId") int userId);
    List<AddressVO> selectOtherAddress(@Param("userId") int userId);
    AddressVO selectAddressDetail(@Param("addressId") long addressId, @Param("userId") int userId);
    int insertAddress(AddressVO address);
    int updateAddress(AddressVO address);
    int deleteAddress(@Param("addressId") long addressId, @Param("userId") int userId);
}