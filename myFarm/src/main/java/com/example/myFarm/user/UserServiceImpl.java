package com.example.myFarm.user;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.OrderVO;
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
    private final OrderMapper orderMapper;

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

    @Override
    public List<OrderVO> getOrderList(int userId) {
        return orderMapper.selectOrderList(userId);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, int userId) {
        return orderMapper.selectOrderDetail(orderId, userId);
    }

    @Override
    public List<ItemVO> getOrderItems(Long orderId) {
        return orderMapper.selectOrderItems(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 롤백 설정
    public void cancelOrder(Long orderId, int userId) {
        OrderVO order = getOrderDetail(orderId, userId);

        if (order == null) {
            throw new IllegalStateException("유효하지 않은 주문 ID입니다.");
        }

        // **[체크] 취소 불가능 상태 확인**
        if ("배송중".equals(order.getStatus()) || "배송완료".equals(order.getStatus())) {
            throw new IllegalStateException("이미 배송이 시작되었거나 완료된 주문은 취소할 수 없습니다.");
        }
        if ("주문 취소".equals(order.getStatus())) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        // 1. 주문 상태 변경
        orderMapper.cancelOrder(orderId, userId);

        // 2. 재고 반환 (OrderMapper에 추가된 쿼리 사용)
        orderMapper.returnInventoryStock(orderId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long placeOrder(OrderVO order, Map<String,String> itemAmounts) {
        int userId = order.getUserId();

        List<ItemVO> finalItemsToOrder = new ArrayList<>();

        for (Map.Entry<String, String> entry : itemAmounts.entrySet()) {
            if (entry.getKey().startsWith("itemAmount_")) {
                try {
                    int itemId = Integer.parseInt(entry.getKey().replace("itemAmount_", ""));
                    int amount = Integer.parseInt(entry.getValue());

                    if (amount <= 0) continue;

                    ItemVO itemDetail = orderMapper.getItemForOrder(itemId);

                    if (itemDetail == null) {
                        throw new IllegalStateException(itemId + "번 상품을 찾을 수 없습니다.");
                    }
                    if (itemDetail.getStockAmount() < amount) {
                        throw new IllegalStateException(itemDetail.getItemName() + "의 재고가 부족합니다. (요청: " + amount + ", 재고: " + itemDetail.getStockAmount() + "개)");
                    }

                    itemDetail.setOrderAmount(amount);
                    finalItemsToOrder.add(itemDetail);

                } catch (NumberFormatException e) {
                    throw new IllegalStateException("유효하지 않은 주문 수량 정보가 포함되어 있습니다.");
                }
            }
        }

        if (finalItemsToOrder.isEmpty()) {
            throw new IllegalStateException("주문할 상품이 선택되지 않았습니다.");
        }

        /*if (order.getPhone() == null || order.getPhone().isEmpty()) {
            throw new IllegalStateException("주문 처리를 위해 배송지 또는 회원 정보에 전화번호가 필요합니다.");
        }*/

        orderMapper.insertOrder(order);
        Long orderId = order.getOrderId();

        int totalAmount = 0;
        String representativeItemName = "";

        for (ItemVO item : finalItemsToOrder) {
            int unitPrice = item.getPrice();

            orderMapper.insertOrderAmount(orderId, item.getItemId(), item.getOrderAmount(), unitPrice);

            int updatedRows = orderMapper.updateInventoryStock(item.getItemId(), item.getOrderAmount());

            if (updatedRows == 0) {
                throw new RuntimeException("재고 업데이트에 실패했습니다. (itemId: " + item.getItemId() + ")");
            }

            if (item.getStockAmount() - item.getOrderAmount() == 0) {
                orderMapper.updateItemStatusToSoldOut(item.getItemId());
            }

            userMapper.deleteCart(userId, item.getItemId());

            totalAmount += item.getPrice() * item.getOrderAmount();

            if (representativeItemName.isEmpty()) {
                representativeItemName = item.getItemName();
            }
        }

        order.setTotalAmount(totalAmount);
        order.setRepresentativeItemName(representativeItemName + (finalItemsToOrder.size() > 1 ? " 외 " + (finalItemsToOrder.size() - 1) + "건" : ""));
        orderMapper.updateOrderSummary(order);

        return order.getOrderId();
    }
}