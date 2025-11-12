package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO; // AddressVO 임포트 추가
import com.example.myFarm.command.CartVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    // --- Cart ---
    List<CartVO> getCartList(int userId);
    int insertCart(CartVO cart);
    int updateCart(CartVO cart);
    int deleteCart(int userId, int itemId);
    int clearCart(int userId);

    // --- Address (추가) ---
    List<AddressVO> selectAddressList(int userId);
    AddressVO selectDefaultAddress(int userId);
    AddressVO selectAddressDetail(long addressId, int userId);
    int insertAddress(AddressVO address);
    int updateAddress(AddressVO address);
    int deleteAddress(long addressId, int userId);
}