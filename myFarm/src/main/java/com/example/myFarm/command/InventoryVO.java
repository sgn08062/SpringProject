package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class InventoryVO {
    private long storId;
    private String storName;
    private int amount;
    private long cropId;
}
