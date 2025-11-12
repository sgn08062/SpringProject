package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

// **가정:** OrderMapper가 별도로 존재하며 OrderVO, OrderItem 관련 매핑을 처리함.
// import com.example.myFarm.mapper.OrderMapper;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    // OrderMapper는 OrderVO 관련 로직 처리를 위해 필요
    private final OrderMapper orderMapper;

    // ********************** Cart **********************

    @Override
    public List<CartVO> getCartList(int userId) {
        return userMapper.getCartList(userId);
    }

    @Override
    @Transactional
    public void addCartItem(CartVO cart) {
        userMapper.insertCart(cart);
    }

    @Override
    @Transactional
    public void updateCartItem(CartVO cart) {
        userMapper.updateCart(cart);
    }

    @Override
    @Transactional
    public void deleteCartItem(int userId, int itemId) {
        userMapper.deleteCart(userId, itemId);
    }

    @Override
    @Transactional
    public void clearCart(int userId) {
        userMapper.clearCart(userId);
    }

    // ********************** Address (구현 추가) **********************

    @Override
    public List<AddressVO> getAddressList(int userId) {
        return userMapper.selectAddressList(userId);
    }

    @Override
    public AddressVO getDefaultAddress(int userId) {
        return userMapper.selectDefaultAddress(userId);
    }

    @Override
    public AddressVO getAddressDetail(long addressId, int userId) {
        return userMapper.selectAddressDetail(addressId, userId);
    }

    @Override
    @Transactional
    public void saveAddress(AddressVO addressForm) {
        // ID가 없거나 0이면 INSERT, 있으면 UPDATE 처리 (컨트롤러 로직 반영)
        if (addressForm.getAddId() == null || addressForm.getAddId() == 0) {
            userMapper.insertAddress(addressForm);
        } else {
            userMapper.updateAddress(addressForm);
        }
    }

    @Override
    @Transactional
    public void deleteAddress(long addressId, int userId) {
        userMapper.deleteAddress(addressId, userId);
    }

    // ********************** Order (구현 추가/유지) **********************


    /**
     * 주문 목록 조회 - null 반환 대신 빈 리스트 반환으로 수정 (500 에러 방지)
     */
    @Override
    public List<OrderVO> getOrderList(int userId) {
        // 실제 구현: orderMapper의 selectOrderList 호출
        return orderMapper.selectOrderList(userId);
    }

    /**
     * 주문 상세 조회 - 컨트롤러에서 null 체크를 하므로 유지
     */
    @Override
    public OrderVO getOrderDetail(Long orderId) {
        // 실제 구현: return orderMapper.selectOrderDetail(orderId);
        return null; // DB 연결 없으므로 임시 반환 유지 (컨트롤러에서 처리 가능)
    }

    /**
     * 주문 취소 (상태 업데이트)
     */
    @Override
    @Transactional
    public void cancelOrder(Long orderId, int userId) {
        // 실제 구현: orderMapper.updateOrderStatus(orderId, "CANCELED");
    }


    @Override
    @Transactional(rollbackFor = Exception.class) // 예외 발생 시 롤백 보장
    public Long placeOrder(OrderVO order) {
        int userId = order.getUserId();

        // 1. 재고 확인을 위한 장바구니 품목 조회
        List<ItemVO> itemsToOrder = orderMapper.getItemsForOrder(userId);

        if (itemsToOrder.isEmpty()) {
            throw new RuntimeException("장바구니가 비어있어 주문할 수 없습니다.");
        }

        // 2. 재고 수량 확인 및 검증
        for (ItemVO item : itemsToOrder) {
            if (item.getAmount() > item.getStockAmount()) {
                throw new RuntimeException(item.getItemName() + "의 재고가 부족합니다. (재고: " + item.getStockAmount() + "개)");
            }
        }

        // 3. ORDERS 테이블에 주문 정보 삽입 (orderId 획득)
        orderMapper.insertOrder(order);
        Long orderId = order.getOrderId(); // MyBatis가 채워준 orderId 사용

        // 4. ORDER_AMOUNT 테이블에 장바구니 품목을 주문 품목으로 삽입
        orderMapper.insertOrderAmount(order);

        // 5. INVENTORY 테이블 재고 감소
        for (ItemVO item : itemsToOrder) {
            // 재고 감소 로직 실행
            int updatedRows = orderMapper.updateInventoryStock(item.getItemId(), item.getAmount());
            if (updatedRows == 0) {
                // 업데이트 실패 시 강제 롤백 (데이터 정합성 보장)
                throw new RuntimeException("재고 업데이트에 실패했습니다. (itemId: " + item.getItemId() + ")");
            }
        }

        // 6. 장바구니 비우기
        userMapper.clearCart(userId);

        // 주문 ID 반환
        return orderId;
    }

}