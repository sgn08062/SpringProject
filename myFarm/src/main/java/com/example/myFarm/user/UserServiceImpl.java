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
    public String getUserName(int userId) {
        return userMapper.selectUserName(userId);
    }

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
    public List<AddressVO> getOtherAddresses(int userId) {
        return userMapper.selectOtherAddress(userId);
    }

    @Override
    public AddressVO getAddressDetail(long addressId, int userId) {
        return userMapper.selectAddressDetail(addressId, userId);
    }

    @Override
    @Transactional
    public void saveAddress(AddressVO addressForm) {
        if (addressForm.getAddressId() == 0) {
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
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, int userId) {
        OrderVO order = getOrderDetail(orderId, userId);

        if (order == null) {
            throw new IllegalStateException("유효하지 않은 주문 ID입니다.");
        }

        if ("배송중".equals(order.getStatus()) || "배송완료".equals(order.getStatus())) {
            throw new IllegalStateException("이미 배송이 시작되었거나 완료된 주문은 취소할 수 없습니다.");
        }
        if ("주문 취소".equals(order.getStatus())) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        orderMapper.cancelOrder(orderId, userId);

        orderMapper.returnInventoryStock(orderId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long placeOrder(OrderVO order, Map<String,String> itemAmounts) {
        int userId = order.getUserId();

        String ordRecipientName;
        long selectedAddressId = order.getAddressId();

        AddressVO defaultAddress = userMapper.selectDefaultAddress(userId);
        long defaultAddressId = (defaultAddress != null) ? defaultAddress.getAddressId() : 0;

        AddressVO selectedAddress = null;

        if (selectedAddressId > 0) {
            selectedAddress = userMapper.selectAddressDetail(selectedAddressId, userId);
        }

        if (selectedAddress == null && selectedAddressId > 0) {
            throw new IllegalStateException("유효하지 않은 배송지 ID입니다.");
        }

        if (selectedAddressId == defaultAddressId) {

            ordRecipientName = getUserName(userId);

            if (defaultAddress == null) {
                throw new IllegalStateException("주문에 필요한 기본 주소 정보가 누락되었습니다.");
            }
            order.setAddress(defaultAddress.getAddress());
            order.setPhone(defaultAddress.getRecipientPhone());

        }
        else if (selectedAddress != null) {

            ordRecipientName = selectedAddress.getRecipientName();

            order.setAddress(selectedAddress.getAddress());
            order.setPhone(selectedAddress.getRecipientPhone());

        }else if (selectedAddressId == 0) {
            // 신규 주소 등록 처리
            //AddressVO newAddress = new AddressVO();
            //newAddress.setUserId(userId);
            //newAddress.setRecipientName(order.getOrdRecipientName());
            //newAddress.setRecipientPhone(order.getPhone());
            //newAddress.setAddress(order.getAddress());
            // OrderVO에 addressName 필드가 있다면 설정
            // newAddress.setAddressName(order.getAddressName());

            // 1. 새 주소를 Address 테이블에 저장 (addressId는 0으로 전송되었으므로 insert 처리)
            //userMapper.insertAddress(newAddress);

            // 2. 주문 정보는 이미 OrderVO에 신규 주소 정보(Address, Phone, OrdRecipientName)가 담겨 있으므로 별도 설정 불필요
            ordRecipientName = order.getOrdRecipientName();

        } else {
            throw new IllegalStateException("유효하지 않은 주소 선택 정보입니다.");
        }

        order.setOrdRecipientName(ordRecipientName);

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

                    if (itemDetail.getItemStatus() != 1) {
                        throw new IllegalStateException(itemDetail.getItemName() + "은 현재 판매 가능한 상태가 아닙니다.");
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

        if (order.getPhone() == null || order.getPhone().isEmpty()) {
            throw new IllegalStateException("주문 처리를 위해 배송지 또는 회원 정보에 전화번호가 필요합니다.");
        }

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