package com.example.myFarm.shop;

import com.example.myFarm.command.ShopVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminShopServiceImpl implements AdminShopService {

    private final AdminShopMapper AdminShopMapper;

    @Override
    public List<ShopVO> getAllItems() {
        return AdminShopMapper.findAllItems();
    }

    @Override
    @Transactional
    public void addItem(ShopVO itemVO) {
        AdminShopMapper.insertItem(itemVO);
    }

    @Override
    @Transactional
    public void updateItem(Long itemId, ShopVO itemVO) {
        itemVO.setItemId(itemId);
        AdminShopMapper.updateItem(itemVO);
    }

//    @Override
//    @Transactional
//    public void deleteItem(Long itemId) {
//        AdminShopMapper.deleteItem(itemId);
//    }

    @Override
    public ShopVO getItemDetail(Long itemId) {
        return AdminShopMapper.getItemDetail(itemId);
    }

    @Override
    @Transactional
    public void updateStatus(Long itemId, Integer status) {
        AdminShopMapper.updateItemStatus(itemId, status);
    }

}