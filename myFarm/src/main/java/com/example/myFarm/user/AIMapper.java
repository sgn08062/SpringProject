package com.example.myFarm.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AIMapper {
    // 1. 농작물 이름으로 작물 존재 여부 및 ID 확인
    @Select("SELECT CROP_ID FROM crop WHERE CROP_NAME = #{cropName}")
    Long findCropIdByName(String cropName);

    // 2. 재고 확인 (inventory 테이블)
    @Select("SELECT IFNULL(SUM(AMOUNT), 0) FROM inventory WHERE CROP_ID = #{cropId}")
    int findAmountByCropId(Long cropId);

    // 3. 수확 시기 확인 (crop 테이블)
    @Select("SELECT GROWTH_TIME FROM crop WHERE CROP_ID = #{cropId}")
    Integer findGrowthTimeByCropId(Long cropId);
}
