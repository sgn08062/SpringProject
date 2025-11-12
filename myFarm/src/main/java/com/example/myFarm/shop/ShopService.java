package com.example.myFarm.shop;

import com.example.myFarm.command.ShopVO;
import com.example.myFarm.command.StatVO;

import java.util.List;

public interface ShopService {
    List<ShopVO> getAllItems();

    void addItem(ShopVO itemVO);

    void updateItem(Long itemId, ShopVO itemVO);

    void deleteItem(Long itemId);

    ShopVO getItemDetail(Long itemId); // 상세 조회

    StatVO getShopStatistics();
}