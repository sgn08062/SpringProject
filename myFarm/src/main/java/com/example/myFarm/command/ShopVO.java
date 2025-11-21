package com.example.myFarm.command;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO {
    private Long itemId;
    private String itemName;
    private Integer price;
    private Integer status;
    private Long storId;
    private Integer inventoryAmount;
}