package com.example.myFarm.crop;

import com.example.myFarm.command.CropVO;

import java.util.List;

public interface AdminCropService {
    int addCrop(CropVO vo);
    int deleteCrop(long cropId);
    int updateCrop(CropVO vo);
    int enableCrop(long cropId);
    int disableCrop(long cropId);

    List<CropVO> getCropList();
    CropVO getCropById(long cropId);
}
