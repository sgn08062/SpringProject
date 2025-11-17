package com.example.myFarm.user;

import com.example.myFarm.command.DummyVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.AddressVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DummyMapper {
    // --- User (AccountService로 분리 완료) ---
    DummyVO selectUserByLoginId(String loginId);
    List<DummyVO> selectAllUsers();
    int insertUser(DummyVO user);
    int updateUser(DummyVO user);
    int deleteUser(Long userId);

    // --- Crop/Inventory (미구현 기능 유지) ---
    List<DummyVO> selectAllCrops();
    int updateCropStatus(DummyVO crop);

    int selectInventoryAmountByCropUuid(String uuid);
    int updateInventoryAmount(DummyVO inventory);

    // --- Shop/Item (UserController가 임시로 사용 중이므로 유지) ---
    List<ItemVO> selectAllShopItems();
    List<ItemVO> searchShopItems(String searchKeyword);
    ItemVO selectShopItemById(Long itemId);

    // --- Address (UserMapper로 분리 완료) ---
    // List<AddressVO> selectAddressesByUserId(Long userId); // ❌ UserMapper로 이전
    // int insertAddress(AddressVO address); // ❌ UserMapper로 이전

    // --- Product (미구현 기능 유지) ---
    DummyVO selectProductDetail(Long prodId);
    int insertProduct(DummyVO product);
}