package com.example.myFarm.shop;

import com.example.myFarm.command.ShopVO;
import java.util.List;

public interface ShopService {
    List<ShopVO> getAllItems();
    void addItem(ShopVO itemVO);
    void updateItem(Long itemId, ShopVO itemVO);
    void deleteItem(Long itemId);
}