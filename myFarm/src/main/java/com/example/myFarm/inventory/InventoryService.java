package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;

import java.util.List;

public interface InventoryService {
    int initForCrop(long cropId);
    void addAmount(long cropId, int amount);


    InventoryVO getByStorId(long storId);
    List<InventoryVO> getAll();
}
