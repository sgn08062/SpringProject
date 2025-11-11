package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {
    int initInventoryForCrop(@Param("cropId") long cropId);
    int increaseInventoryAmount(@Param("cropId") long cropId,
                                @Param("amount") int amount);
    int insertInventory(@Param("cropId") long cropId,
                        @Param("amount") int amount);


    InventoryVO selectByCropId(@Param("cropId") long cropId);
    List<InventoryVO> selectAll();
}