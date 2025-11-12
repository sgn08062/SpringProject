package com.example.myFarm.user;

import com.example.myFarm.command.DummyVO;
import com.example.myFarm.command.ItemVO;    // 💡 독립 ItemVO 임포트 추가
import com.example.myFarm.command.AddressVO; // 💡 독립 AddressVO 임포트 추가
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DummyMapper {
    DummyVO selectUserByLoginId(String loginId);
    List<DummyVO> selectAllUsers();
    int insertUser(DummyVO user);
    int updateUser(DummyVO user);
    int deleteUser(Long userId);

    List<DummyVO> selectAllCrops();
    int updateCropStatus(DummyVO crop);

    int selectInventoryAmountByCropUuid(String uuid);
    int updateInventoryAmount(DummyVO inventory);

    List<ItemVO> selectAllShopItems(); // 💡 타입 변경: DummyVO.ItemVO -> ItemVO
    List<ItemVO> searchShopItems(String searchKeyword); // 💡 타입 변경
    ItemVO selectShopItemById(Long itemId); // 💡 타입 변경

    List<AddressVO> selectAddressesByUserId(Long userId); // 💡 타입 변경: DummyVO.AddressVO -> AddressVO
    int insertAddress(AddressVO address); // 💡 타입 변경

    DummyVO selectProductDetail(Long prodId);
    int insertProduct(DummyVO product);
}