package com.example.myFarm.uorder; // uorder 패키지로 가정

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.util.Criteria;

import java.util.List;
import java.util.Map;

public interface OrderService {

    // ⭐ [추가됨] Default 수령인 이름 설정을 위해 USERS 테이블에서 USER_NAME을 조회
    String getUserName(int userId);

    // --- 주소록 관련 ---
    List<AddressVO> getAddressList(int userId);
    AddressVO getDefaultAddress(int userId);
    // ⭐ 추가: 기본 주소를 제외한 기타 주소 목록 조회
    List<AddressVO> getOtherAddresses(int userId);
    AddressVO getAddressDetail(long addressId, int userId);
    void saveAddress(AddressVO addressForm);
    void deleteAddress(long addressId, int userId);

    // --- Order ---
    Long placeOrder(OrderVO order, Map<String, String> itemAmounts);
    List<OrderVO> getOrderList(int userId);
    OrderVO getOrderDetail(Long orderId, int userId);
    List<ItemVO> getOrderItems(Long orderId);
    void cancelOrder(Long orderId, int userId);

    // --- page ---
    Map<String, Object> getOrderListWithPaging(int userId, Criteria cri);
}