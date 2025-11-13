package com.example.myFarm.shop;

import com.example.myFarm.command.ShopVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminShopMapper {
    List<ShopVO> findAllItems();
    void insertItem(ShopVO itemVO);
    void updateItem(ShopVO itemVO);
    void deleteItem(Long itemId);
    ShopVO getItemDetail(Long itemId);

    void updateItemStatus(@Param("itemId") Long itemId,
                          @Param("status") Integer status);

}