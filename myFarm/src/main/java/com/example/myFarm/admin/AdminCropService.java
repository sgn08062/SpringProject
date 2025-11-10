package com.example.myFarm.admin;

import com.example.myFarm.command.CropVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AdminCropService {
    int addCrop(CropVO vo);

    int deleteCrop(String uuid);
    int enableCrop(String uuid);
    int disableCrop(String uuid);
}
