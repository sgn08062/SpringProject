package com.example.myFarm.shop;

import com.example.myFarm.command.ShopVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    // 매퍼 이름이 ShopMapper로 변경됨
    private final ShopMapper shopMapper;

    @Override
    public List<ShopVO> getAllItems() {
        return shopMapper.findAllItems();
    }

    @Override
    @Transactional
    public void addItem(ShopVO itemVO) {
        shopMapper.insertItem(itemVO);
    }

    @Override
    @Transactional
    public void updateItem(Long itemId, ShopVO itemVO) {
        itemVO.setItemId(itemId);
        shopMapper.updateItem(itemVO);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        shopMapper.deleteItem(itemId);
    }
}