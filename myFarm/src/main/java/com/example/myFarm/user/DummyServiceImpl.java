package com.example.myFarm.user;

// import com.example.myFarm.user.DummyMapper; // ❌ DummyMapper import 주석 처리
// import com.example.myFarm.command.DummyVO; // ❌ DummyVO import 주석 처리
// import com.example.myFarm.command.ItemVO; // ❌ ItemVO import 주석 처리
// import org.springframework.beans.factory.annotation.Autowired; // ❌ Autowired import 주석 처리
import org.springframework.stereotype.Service;
// import java.util.List; // ❌ List import 주석 처리

@Service
public class DummyServiceImpl implements DummyService {
    /*
    // private final DummyMapper dummyMapper; // ❌ 필드 주석 처리

    // @Autowired // ❌ 생성자 주석 처리
    // public DummyServiceImpl(DummyMapper dummyMapper) {
    //     this.dummyMapper = dummyMapper;
    // }

    // ✅ 유지: 상품 조회 기능 (UserController가 임시로 사용)
    @Override
    public List<ItemVO> getAllShopItems() {
        // return dummyMapper.selectAllShopItems(); // ❌ 구현 내용 주석 처리
        return null;
    }

    @Override
    public List<ItemVO> searchAndSortShopItems(String searchKeyword, String sortField) {
        // if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
        //     return dummyMapper.selectAllShopItems();
        // }
        // return dummyMapper.searchShopItems(searchKeyword); // ❌ 구현 내용 주석 처리
        return null;
    }

    @Override
    public ItemVO getShopItemDetail(Long itemId) {
        // return dummyMapper.selectShopItemById(itemId); // ❌ 구현 내용 주석 처리
        return null;
    }

    // ❌ 제거/주석 처리: User 관련 기능 (AccountService로 분리 완료)
     @Override
    public DummyVO getUserInfo(String loginId) {
        return null;
    }

    // ✅ 유지: 미구현된 다른 더미 기능
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
    public int getInventoryAmount(String cropUuid) {
        // 💡 더미 구현
        return 0;
    }

    @Override
    public int updateInventory(String cropUuid, int newAmount) {
        // 💡 더미 구현
        return 0;
    }
    */
}