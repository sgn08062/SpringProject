package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CropVO {
    private int cropId;
    private String cropName;
    private String regDate;
    private int status;
    private int growthTime;
    private int quantity;
    private String unitName;
    private int elapsedTick;
}