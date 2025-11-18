package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private Long orderId;        // 주문 번호 (ORDERS.ORDER_ID)
    private String customerName; // 고객명 (USERS.USER_NAME 또는 수령인 이름)
    private String orderDate;    // 주문일 (ORDERS.ORDER_DATE)
    private Long totalAmount;    // 금액 (ORDERS.TOTAL_AMOUNT)
    private String status;       // 상태 (ORDERS.STATUS)
}