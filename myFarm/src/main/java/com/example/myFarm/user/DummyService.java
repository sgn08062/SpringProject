package com.example.myFarm.user;

import com.example.myFarm.command.DummyVO;
import com.example.myFarm.command.ItemVO;// 💡 ItemVO 임포트 추가
import java.util.List;

public interface DummyService {
    List<ItemVO> getAllShopItems(); // 💡 타입 변경
    List<ItemVO> searchAndSortShopItems(String searchKeyword, String sortField); // 💡 타입 변경
    ItemVO getShopItemDetail(Long itemId); // 💡 타입 변경
    DummyVO getProductDetail(Long prodId);

    // DummyVO.AddressVO 대신 AddressVO를 독립적으로 사용한다고 가정
    // List<DummyVO.AddressVO> getUserAddresses(Long userId); // 이 메서드는 UserService로 분리될 가능성이 높으나 현재는 유지
    // int addAddress(DummyVO.AddressVO address); // 이 메서드는 UserService로 분리될 가능성이 높으나 현재는 유지

    List<ItemVO> getOrderItems(Long orderId); // 💡 타입 변경
    List<ItemVO> getCheckoutItems(int userId); // 💡 타입 변경

    // ... (나머지 DummyVO 관련 메서드)
    DummyVO getUserInfo(String loginId);
    int getInventoryAmount(String cropUuid);
    int updateInventory(String cropUuid, int newAmount);
}