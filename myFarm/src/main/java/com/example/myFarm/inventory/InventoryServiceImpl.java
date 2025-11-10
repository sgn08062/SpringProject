package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public int initForCrop(String uuid) {
        return inventoryMapper.initInventoryForCrop(uuid);
    }

    @Override
    public void addAmount(String uuid, int amount) {
        if(amount<=0) throw new IllegalArgumentException("Amount must be greater than 0");
        int updated = inventoryMapper.increaseInventoryAmount(uuid, amount);
        if(updated==0) {
            inventoryMapper.insertInventory(uuid, 0);
            inventoryMapper.increaseInventoryAmount(uuid, amount);
        }
    }

    @Override
    public InventoryVO getByCropId(String uuid) {
        return inventoryMapper.selectByCropId(uuid);
    }

    @Override
    public List<InventoryVO> getAll() {
        return inventoryMapper.selectAll();
    }
}
