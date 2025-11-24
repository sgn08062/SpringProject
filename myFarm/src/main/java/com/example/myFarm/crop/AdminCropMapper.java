package com.example.myFarm.crop;

import com.example.myFarm.command.CropVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminCropMapper {
    int addCrop(CropVO vo);

    int deleteCrop(@Param("cropId") long cropId);
    int enableCrop(@Param("cropId") long cropId);
    int disableCrop(@Param("cropId") long cropId);

    List<CropVO> getCropList();
    CropVO getCropById(long cropId);
    int updateCrop(CropVO vo);

    int tickActiveCrops();
    List<Map<String, Object>> selectMatureCrops();
    int resetCropAfterHarvest(@Param("cropId") long cropId);
}
