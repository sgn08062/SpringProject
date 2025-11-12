package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.OrderVO;

import java.util.List;

public interface UserService {
    // 상품 관련 메서드 모두 제거됨 (DummyService로 분리)

    // 주소록 관련 (유지)
    List<AddressVO> getAddressList(int userId);
    AddressVO getDefaultAddress(int userId);
    AddressVO getAddressDetail(long addressId, int userId);
    void saveAddress(AddressVO addressForm);
    void deleteAddress(long addressId, int userId);

    // cart (유지)
    List<CartVO> getCartList(int userId);
    void addCartItem(CartVO cart);
    void updateCartItem(CartVO cart);
    void deleteCartItem(int userId, int itemId);
    void clearCart(int userId);

    // order (유지)
    Long placeOrder(OrderVO order);
    List<OrderVO> getOrderList(int userId);
    OrderVO getOrderDetail(Long orderId);
    void cancelOrder(Long orderId, int userId);
}