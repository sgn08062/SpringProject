package com.example.myFarm.cart.mapper;

import com.example.myFarm.cart.dto.CartDTO;
import com.example.myFarm.cart.dto.CartViewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CartMapperSub {

    /**
     * [v12] 특정 사용자의 장바구니 목록 전체 조회 (SHOP과 JOIN)
     * @param userId
     * @return
     */
    List<CartViewDTO> findCartItemsByUserId(Long userId);

    /**
     * [v12] 장바구니에 특정 아이템이 있는지 확인 (복합키로 조회)
     * @param userId
     * @param itemId
     * @return
     */
    Optional<CartDTO> findCartItemByCompositeKey(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId
    );

    /**
     * [v12] 장바구니에 새 아이템 삽입
     * @param item
     */
    void insertCartItem(CartDTO item);

    /**
     * [v12] 장바구니 아이템 수량 변경
     * @param item
     */
    void updateCartItemAmount(CartDTO item);

    /**
     * [v12] 장바구니 아이템 삭제
     * @param userId
     * @param itemId
     */
    void deleteCartItem(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId
    );

    Long findUserIdByLoginId(String loginId);
}