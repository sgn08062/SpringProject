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
    private int amount; // shop 재고수량
    private int stockAmount; // 창고 재고수량
}