package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;

import java.util.List;

public interface InventoryService {
    int initForCrop(String uuid);
    void addAmount(String uuid, int amount);

    InventoryVO getByCropId(String uuid);
    List<InventoryVO> getAll();
}
