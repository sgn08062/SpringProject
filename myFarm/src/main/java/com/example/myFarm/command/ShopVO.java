package com.example.myFarm.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopVO {

    private Long itemId;
    private String itemName;
    private Integer price;
    private Long storId;
}