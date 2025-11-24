package com.example.myFarm.crop;


import com.example.myFarm.command.CropVO;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminCropServiceImpl implements AdminCropService {
    @Autowired
    private AdminCropMapper adminCropMapper;
    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public int addCrop(CropVO vo) {
        int r = adminCropMapper.addCrop(vo); // useGeneratedKeys로 cropId 채워짐
        if (r == 1) {
            r = inventoryService.initForCrop(vo.getCropId(), vo.getCropName());
        }
        return r;
    }

    @Override
    public int deleteCrop(long cropId) {
        return adminCropMapper.deleteCrop(cropId);
    }

    @Override
    public int updateCrop(CropVO vo) {
        return adminCropMapper.updateCrop(vo);
    }

    @Override
    public int enableCrop(long cropId) {
        return adminCropMapper.enableCrop(cropId);
    }


    @Override
    public int disableCrop(long cropId) {
        return adminCropMapper.disableCrop(cropId);
    }

    @Override
    public List<CropVO> getCropList() {
        return adminCropMapper.getCropList();
    }

    @Override
    public CropVO getCropById(long cropId) {
        return adminCropMapper.getCropById(cropId);
    }
}
