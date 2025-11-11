package com.example.myFarm.admin;

import com.example.myFarm.command.CropVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AdminCropService {
    int addCrop(CropVO vo);

    int deleteCrop(long cropId);
    int enableCrop(long cropId);
    int disableCrop(long cropId);

    List<CropVO> getCropList();
}
