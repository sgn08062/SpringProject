package com.example.myFarm.user;

// import com.example.myFarm.cart.CartService; // ❌ 주문/주소 로직 분리로 제거
// import com.example.myFarm.command.AddressVO; // ❌ 주소 로직 제거
import com.example.myFarm.command.CartVO;
// import com.example.myFarm.command.ItemVO; // ❌ 주문 로직 제거
// import com.example.myFarm.command.OrderVO; // ❌ 주문 로직 제거
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    // private final OrderMapper orderMapper; // ❌ 주문 로직 분리로 제거
    // private final CartService cartService; // ❌ 주문 로직 분리로 제거

    @Override
    public String getUserName(int userId) {
        return userMapper.selectUserName(userId);
    }

    /* ❌ 삭제: 장바구니 조회 및 수정 로직은 CartService로 분리
    @Override
    public List<CartVO> getCartList(int userId) {
        // return userMapper.getCartList(userId);
        return null;
    }

    @Override
    @Transactional
    public void addCartItem(CartVO cart) {
        // userMapper.insertCart(cart);
    }

    @Override
    @Transactional
    public void updateCartItem(CartVO cart) {
        // userMapper.updateCart(cart);
    }

    @Override
    @Transactional
    public void deleteCartItem(int userId, int itemId) {
        // userMapper.deleteCart(userId, itemId);
    }

    @Override
    @Transactional
    public void clearCart(int userId) {
        // userMapper.clearCart(userId);
    }
    */

    /* ❌ 삭제: 주소 관련 로직은 OrderService로 분리
    @Override
    public List<AddressVO> getAddressList(int userId) {
        // return userMapper.selectAddressList(userId);
        return null;
    }

    @Override
    public AddressVO getDefaultAddress(int userId) {
        // return userMapper.selectDefaultAddress(userId);
        return null;
    }

    @Override
    public List<AddressVO> getOtherAddresses(int userId) {
        // return userMapper.selectOtherAddress(userId);
        return null;
    }

    @Override
    public AddressVO getAddressDetail(long addressId, int userId) {
        // return userMapper.selectAddressDetail(addressId, userId);
        return null;
    }

    @Override
    @Transactional
    public void saveAddress(AddressVO addressForm) {
        // if (addressForm.getAddressId() == 0) {
        //     userMapper.insertAddress(addressForm);
        // } else {
        //     userMapper.updateAddress(addressForm);
        // }
    }

    @Override
    @Transactional
    public void deleteAddress(long addressId, int userId) {
        // userMapper.deleteAddress(addressId, userId);
    }
    */

    /* ❌ 삭제: 주문 관련 로직은 OrderService로 분리
    @Override
    public List<OrderVO> getOrderList(int userId) {
        // return orderMapper.selectOrderList(userId);
        return null;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, int userId) {
        // return orderMapper.selectOrderDetail(orderId, userId);
        return null;
    }

    @Override
    public List<ItemVO> getOrderItems(Long orderId) {
        // return orderMapper.selectOrderItems(orderId);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, int userId) {
        // OrderVO order = getOrderDetail(orderId, userId);

        // if (order == null) {
        //     throw new IllegalStateException("유효하지 않은 주문 ID입니다.");
        // }

        // if ("배송중".equals(order.getStatus()) || "배송완료".equals(order.getStatus())) {
        //     throw new IllegalStateException("이미 배송이 시작되었거나 완료된 주문은 취소할 수 없습니다.");
        // }
        // if ("주문 취소".equals(order.getStatus())) {
        //     throw new IllegalStateException("이미 취소된 주문입니다.");
        // }

        // orderMapper.cancelOrder(orderId, userId);
        // orderMapper.returnInventoryStock(orderId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long placeOrder(OrderVO order, Map<String,String> itemAmounts) {
        // (주문 로직 전체 삭제)
        return null;
    }
    */
}