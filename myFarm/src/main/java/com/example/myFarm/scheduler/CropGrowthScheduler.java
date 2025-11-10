package com.example.myFarm.scheduler;

import com.example.myFarm.admin.AdminCropMapper;
import com.example.myFarm.inventory.InventoryMapper;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CropGrowthScheduler {

    @Autowired
    private AdminCropMapper adminCropMapper;
    @Autowired
    private InventoryService inventoryService;

    @Scheduled(fixedRate = 1000L)
    public void tickAndHarvest(){
        // enable된 작물 1틱 증가
        adminCropMapper.tickActiveCrops();

        // 성숙 대상 조회
        List<Map<String, Object>> ready = adminCropMapper.selectMatureCrops();

        // 수확
        for(Map<String, Object> row: ready){
            String uuid =  (String) row.get("uuid");
            int amount = Integer.parseInt(row.get("amount").toString());

            inventoryService.addAmount(uuid, amount);
            adminCropMapper.resetCropAfterHarvest(uuid);
        }
    }
}
