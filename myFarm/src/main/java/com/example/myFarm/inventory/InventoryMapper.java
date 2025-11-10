package com.example.myFarm.inventory;

import com.example.myFarm.command.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {
    int initInventoryForCrop(@Param("uuid") String uuid);
    int increaseInventoryAmount(@Param("uuid") String uuid,
                                @Param("amount") int amount);
    void insertInventory(@Param("uuid") String uuid,
                         @Param("amount") int amount);

    InventoryVO selectByCropId(@Param("uuid") String uuid);
    List<InventoryVO> selectAll();
}
