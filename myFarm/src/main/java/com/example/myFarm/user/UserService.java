package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.ItemVO;

import java.util.List;
import java.util.Map;

public interface UserService {
    // --- 주소록 관련 ---
    List<AddressVO> getAddressList(int userId);
    AddressVO getDefaultAddress(int userId);
    AddressVO getAddressDetail(long addressId, int userId);
    void saveAddress(AddressVO addressForm);
    void deleteAddress(long addressId, int userId);

    // --- Cart ---
    List<CartVO> getCartList(int userId);
    void addCartItem(CartVO cart);
    void updateCartItem(CartVO cart);
    void deleteCartItem(int userId, int itemId);
    void clearCart(int userId);

    // --- Order ---
    Long placeOrder(OrderVO order, Map<String, String> itemAmounts);
    List<OrderVO> getOrderList(int userId);
    OrderVO getOrderDetail(Long orderId, int userId);
    List<ItemVO> getOrderItems(Long orderId);
    void cancelOrder(Long orderId, int userId);
}