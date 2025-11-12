package com.example.myFarm.cart.mapper;

import com.example.myFarm.cart.dto.CartDTO;
import com.example.myFarm.cart.dto.CartItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper // Spring이 이 인터페이스를 MyBatis Mapper로 인식하게 함
public interface CartMapper {

    // 1. memberId로 장바구니 찾기
    Optional<CartDTO> findCartByMemberId(Long memberId);

    // 2. 장바구니 생성 (회원가입 시)
    void createCart(CartDTO cart);

    // 3. 장바구니(cartId)에 특정 상품(productId)이 있는지 확인
    Optional<CartItemDTO> findCartItemByCartIdAndProductId(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId
    );

    // 4. 장바구니에 새 아이템 추가
    void insertCartItem(CartItemDTO cartItem);

    // 5. 장바구니 아이템 수량 변경
    void updateCartItemQuantity(CartItemDTO cartItem);

    // 6. 특정 장바구니의 모든 아이템 조회
    List<CartItemDTO> findCartItemsByCartId(Long cartId);

    // 7. 장바구니 아이템 삭제
    void deleteCartItem(Long cartItemId);
}