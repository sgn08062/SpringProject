package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {
    int initInventoryForCrop(@Param("cropId") long cropId,
                             @Param("cropName") String cropName);
    int increaseInventoryAmount(@Param("cropId") long cropId,
                                @Param("amount") long amount);

    InventoryVO getByStorId(@Param("storId") long storId);
    List<InventoryVO> selectAll();
}