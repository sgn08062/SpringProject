package com.example.myFarm.admin;

import com.example.myFarm.command.CropVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminCropMapper {
    public int addCrop(CropVO vo);
    public int deleteCrop(long cropId);
    public int enableCrop(long cropId);
    public int disableCrop(long cropId);
}
