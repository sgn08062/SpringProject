package com.example.myFarm.user;

import com.example.myFarm.user.DummyMapper;
import com.example.myFarm.command.DummyVO;
import com.example.myFarm.command.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DummyServiceImpl implements DummyService {

    private final DummyMapper dummyMapper;

    @Autowired
    public DummyServiceImpl(DummyMapper dummyMapper) {
        this.dummyMapper = dummyMapper;
    }

    @Override
    public List<ItemVO> getAllShopItems() {
        return dummyMapper.selectAllShopItems();
    }

    @Override
    public List<ItemVO> searchAndSortShopItems(String searchKeyword, String sortField) {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            return dummyMapper.selectAllShopItems();
        }
        return dummyMapper.searchShopItems(searchKeyword);
    }

    @Override
    public ItemVO getShopItemDetail(Long itemId) {
        return dummyMapper.selectShopItemById(itemId);
    }

    @Override
    public DummyVO getProductDetail(Long prodId) {
        // 💡 더미 구현
        return null;
    }

    @Override
    public List<ItemVO> getOrderItems(Long orderId) {
        // 💡 더미 구현
        return null;
    }

    @Override
    public List<ItemVO> getCheckoutItems(int userId) {
        // 💡 더미 구현
        return null;
    }

    @Override
    public DummyVO getUserInfo(String loginId) {
        // 💡 더미 구현
        return null;
    }

    @Override
    public int getInventoryAmount(String cropUuid) {
        // 💡 더미 구현
        return 0;
    }

    @Override
    public int updateInventory(String cropUuid, int newAmount) {
        // 💡 더미 구현
        return 0;
    }
}