package com.example.myFarm.shop;

import com.example.myFarm.command.ShopVO;
import com.example.myFarm.command.StatVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ShopMapper { // ShopItemMapper -> ShopMapper로 변경됨
    List<ShopVO> findAllItems();
    void insertItem(ShopVO itemVO);
    void updateItem(ShopVO itemVO);
    void deleteItem(Long itemId);
    ShopVO getItemDetail(Long itemId); // 상세 조회
    StatVO getShopStatistics();
}