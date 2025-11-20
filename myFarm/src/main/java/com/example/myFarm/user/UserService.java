package com.example.myFarm.user;

// import com.example.myFarm.command.AddressVO; // ❌ Address 로직 분리로 제거
import com.example.myFarm.command.CartVO;
// import com.example.myFarm.command.OrderVO; // ❌ Order 로직 분리로 제거
// import com.example.myFarm.command.ItemVO; // ❌ Order 로직 분리로 제거

import java.util.List;
import java.util.Map;

public interface UserService {

    // ⭐ [남아있는 메서드] 사용자 이름 조회 (OrderService에서도 UserMapper를 통해 직접 조회 가능)
    String getUserName(int userId);

    // --- 주소록 관련 ---
    /* ❌ 삭제: OrderService로 분리
    List<AddressVO> getAddressList(int userId);
    AddressVO getDefaultAddress(int userId);
    List<AddressVO> getOtherAddresses(int userId);
    AddressVO getAddressDetail(long addressId, int userId);
    void saveAddress(AddressVO addressForm);
    void deleteAddress(long addressId, int userId);
    */

    // --- Cart ---
    /* ❌ 삭제: CartService로 분리
    List<CartVO> getCartList(int userId);
    void addCartItem(CartVO cart);
    void updateCartItem(CartVO cart);
    void deleteCartItem(int userId, int itemId);
    void clearCart(int userId);
    */

    // --- Order ---
    /* ❌ 삭제: OrderService로 분리
    Long placeOrder(OrderVO order, Map<String, String> itemAmounts);
    List<OrderVO> getOrderList(int userId);
    OrderVO getOrderDetail(Long orderId, int userId);
    List<ItemVO> getOrderItems(Long orderId);
    void cancelOrder(Long orderId, int userId);
    */
}