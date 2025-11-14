package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ItemVO {
    private int itemId;
    private String itemName;
    private int price;
    private int amount; // shop 재고수량 (Shop.AMOUNT)
    private int stockAmount; // 창고 재고수량 (Inventory.AMOUNT)
    private int itemStatus; // sold 상태 체크
    private int orderAmount; // ✨ 주문 당시 수량 (ORDER_AMOUNT.AMOUNT)
}