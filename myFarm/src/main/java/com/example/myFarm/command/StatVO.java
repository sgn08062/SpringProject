package com.example.myFarm.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatVO {

    private Long totalSales;    // 총 매출
    private Long totalOrders;   // 총 주문 건수
    private Long totalItems;    // 등록 상품 수
    private Long avgOrderAmount; // 평균 주문액
}