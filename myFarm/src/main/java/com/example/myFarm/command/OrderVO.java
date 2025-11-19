package com.example.myFarm.command;


import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderVO {
    private List<ItemVO> orderItems;

    private Long orderId;
    private String status;
    private String customerName; // 고객명 (USERS.USER_NAME 또는 수령인 이름)


    private String ordRecipientName; // 주문 시점의 수령인 이름 (ORDERS 테이블에 저장)

    //  추가: 배송지 관련 필드 (주문 시 선택/입력된 주소 정보)
    private int addressId;          // ADD_ID (기존 주소 사용 시 필요. int 타입 유지)
    //private String recipientName;   // 받는 사람 이름 (새 주소/기본 주소 모두 필요)
    private String addressName;     // 배송지명 (새 주소/기본 주소 모두 필요)

    private String address;
    private String phone;
    private int userId;

    private LocalDateTime orderDate;
    private long totalAmount;
    private String representativeItemName;

}