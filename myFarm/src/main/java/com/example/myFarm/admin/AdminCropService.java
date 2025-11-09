package com.example.myFarm.admin;

import com.example.myFarm.command.CropVO;

public interface AdminCropService {
    public int addCrop(CropVO vo);
    public int deleteCrop(long cropId);
}
