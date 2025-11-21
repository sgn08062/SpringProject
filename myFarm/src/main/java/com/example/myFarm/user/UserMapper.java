package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.CartVO; // CartVO는 AddressVO와 OrderVO에서 여전히 사용될 수 있으므로 import는 유지합니다.
import com.example.myFarm.command.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {

    // ⭐ [추가 필요] USER_NAME을 조회하는 메서드 (Default 수령인 이름 설정용)
    String selectUserName(@Param("userId") int userId);

    // --- Cart ---
    /* ❌ 삭제: 장바구니 로직은 CartMapper로 이동
    List<CartVO> getCartList(@Param("userId") int userId);
    int insertCart(CartVO cart);
    int updateCart(CartVO cart);
    int deleteCart(@Param("userId") int userId, @Param("itemId") int itemId);
    int clearCart(@Param("userId") int userId);
    */
    // *주의: 주문 로직(placeOrder)에서 장바구니 삭제가 필요하면, 해당 로직만 CartService 호출로 변경해야 합니다.*

    // --- Address ---
    List<AddressVO> selectAddressList(@Param("userId") int userId);
    AddressVO selectDefaultAddress(@Param("userId") int userId);
    // ⭐ 추가: 기본 주소 외의 주소 목록 조회
    List<AddressVO> selectOtherAddress(@Param("userId") int userId);
    AddressVO selectAddressDetail(@Param("addressId") long addressId, @Param("userId") int userId);
    int insertAddress(AddressVO address);
    int updateAddress(AddressVO address);
    int deleteAddress(@Param("addressId") long addressId, @Param("userId") int userId);
    public UserVO selectUserInfo(int userId);
}