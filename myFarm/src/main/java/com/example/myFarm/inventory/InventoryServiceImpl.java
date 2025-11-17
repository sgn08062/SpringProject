package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public int initForCrop(long cropId) {
        return inventoryMapper.initInventoryForCrop(cropId);
    }

    @Override
    @Transactional
    public void addAmount(long cropId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        int updated = inventoryMapper.increaseInventoryAmount(cropId, amount);
        if (updated == 0) {
            inventoryMapper.insertInventory(cropId, 0);
            inventoryMapper.increaseInventoryAmount(cropId, amount);
        }
    }

    @Override
    public InventoryVO getByStorId(long storId) {
        return inventoryMapper.getByStorId(storId);
    }

    @Override
    public List<InventoryVO> getAll() {
        return inventoryMapper.selectAll();
    }
}
