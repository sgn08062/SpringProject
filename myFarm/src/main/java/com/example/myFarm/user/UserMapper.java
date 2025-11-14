package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    // --- Cart ---
    List<CartVO> getCartList(@Param("userId") int userId);
    int insertCart(CartVO cart);
    int updateCart(CartVO cart);
    int deleteCart(@Param("userId") int userId, @Param("itemId") int itemId);
    int clearCart(@Param("userId") int userId);

    // --- Address (유지) ---
    List<AddressVO> selectAddressList(@Param("userId") int userId);
    AddressVO selectDefaultAddress(@Param("userId") int userId);
    AddressVO selectAddressDetail(@Param("addressId") long addressId, @Param("userId") int userId);
    int insertAddress(AddressVO address);
    int updateAddress(AddressVO address);
    int deleteAddress(@Param("addressId") long addressId, @Param("userId") int userId);

    // ⭐ 추가: USER_ID로 전화번호를 조회하는 메서드
    String selectUserPhone(@Param("userId") int userId);
}