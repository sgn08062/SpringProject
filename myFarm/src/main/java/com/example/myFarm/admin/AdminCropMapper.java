package com.example.myFarm.admin;

import com.example.myFarm.command.CropVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminCropMapper {
    public void addCrop(CropVO vo);

}
