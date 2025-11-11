package com.example.myFarm.scheduler;

import com.example.myFarm.admin.AdminCropMapper;
import com.example.myFarm.inventory.InventoryMapper;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class CropGrowthScheduler {

    @Autowired
    private AdminCropMapper adminCropMapper;
    @Autowired
    private InventoryService inventoryService;

    @Scheduled(fixedRate = 1000L)
    @Transactional
    public void tickAndHarvest(){
        // enable된 작물 1틱 증가
        adminCropMapper.tickActiveCrops();

        // 성숙 대상 조회
        List<Map<String, Object>> ready = adminCropMapper.selectMatureCrops();

        // 수확
        for(Map<String, Object> row: ready){
            long cropId = Long.parseLong(row.get("cropId").toString());
            int quantity = Integer.parseInt(row.get("quantity").toString());

            inventoryService.addAmount(cropId, quantity);
            adminCropMapper.resetCropAfterHarvest(cropId);
        }
    }
}
