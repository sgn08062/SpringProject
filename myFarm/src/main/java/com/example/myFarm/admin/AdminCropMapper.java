package com.example.myFarm.admin;

import com.example.myFarm.command.CropVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminCropMapper {
    int addCrop(CropVO vo);
    int deleteCrop(@Param("uuid") String uuid);
    int enableCrop(@Param("uuid") String uuid);
    int disableCrop(@Param("uuid") String uuid);

    void tickActiveCrops();
    List<Map<String, Object>> selectMatureCrops();
    void resetCropAfterHarvest(@Param("uuid") String uuid);
}
