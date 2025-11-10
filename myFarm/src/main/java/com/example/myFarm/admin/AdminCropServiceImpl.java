package com.example.myFarm.admin;


import com.example.myFarm.command.CropVO;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminCropServiceImpl implements AdminCropService {
    @Autowired
    private AdminCropMapper adminCropMapper;
    @Autowired
    private InventoryService inventoryService;

    @Override
    public int addCrop(CropVO vo) {
        String uuid = UUID.randomUUID().toString();
        vo.setUuid(uuid);
        System.out.println("uuid:"+uuid);
        int result = adminCropMapper.addCrop(vo);
        System.out.println(vo.getUuid());
        if(result == 1) result = inventoryService.initForCrop(uuid);
        return result;
    }

    @Override
    public int deleteCrop(String uuid) {
        return adminCropMapper.deleteCrop(uuid);
    }

    @Override
    public int enableCrop(String uuid) {
        return adminCropMapper.enableCrop(uuid);
    }

    @Override
    public int disableCrop(String uuid) {
        return adminCropMapper.disableCrop(uuid);
    }
}
