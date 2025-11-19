package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;

import java.util.List;

public interface InventoryService {
    int initForCrop(long cropId, String cropName);
    void addAmount(long cropId, long amount);


    InventoryVO getByStorId(long storId);
    List<InventoryVO> getAll();
}
