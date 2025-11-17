package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartVO {
    private int userId;
    private int itemId;
    private int amount;
    private String itemName;
    private int price;
    private int stockAmount;
    private String uuid;
    private String unitName;
}