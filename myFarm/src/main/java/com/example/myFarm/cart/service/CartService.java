package com.example.myFarm.cart.service;

import com.example.myFarm.cart.dto.CartDTO;
import com.example.myFarm.cart.dto.CartViewDTO;
import com.example.myFarm.cart.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;

    /**
     * [v12] 장바구니 목록 조회
     * @param userId
     * @return
     */
    public List<CartViewDTO> getCartItems(Long userId) {
        // Mapper가 SHOP과 JOIN하여 이름/가격을 모두 가져옴
        return cartMapper.findCartItemsByUserId(userId);
    }

    /**
     * [v12] 장바구니에 아이템 추가 (또는 수량 변경)
     *
     * @param userId
     * @param itemId (script.js에서 보낸 productId)
     * @param quantity (추가할 수량, 음수일 수 있음)
     */
    @Transactional
    public void addItemToCart(Long userId, Long itemId, int quantity) {

        // 1. 이 상품이 이미 장바구니에 있는지 확인
        Optional<CartDTO> existingItem = cartMapper.findCartItemByCompositeKey(userId, itemId);

        if (existingItem.isPresent()) {
            // 2. (A) 이미 있는 경우: 수량 업데이트
            CartDTO item = existingItem.get();
            int newAmount = item.getAmount() + quantity;

            if (newAmount <= 0) {
                // 3. (A-1) 새 수량이 0 이하면: 아이템 삭제
                cartMapper.deleteCartItem(userId, itemId);
            } else {
                // 3. (A-2) 새 수량이 0보다 크면: 수량 업데이트
                item.setAmount(newAmount);
                cartMapper.updateCartItemAmount(item);
            }

        } else {
            // 2. (B) 없는 경우:
            if (quantity > 0) {
                // 3. (B-1) 추가할 수량이 양수일 때만: 새 아이템 삽입
                CartDTO newItem = new CartDTO(userId, itemId, quantity);
                cartMapper.insertCartItem(newItem);
            }
            // (추가할 수량이 음수면 아무것도 하지 않음)
        }
    }

    /**
     * [v12] 장바구니 아이템 완전 삭제
     * (이 메서드는 script.js의 '-' 버튼이 아닌,
     * 'X' 버튼 같은 완전 삭제용으로 사용될 수 있음)
     * * @param userId
     * @param itemId
     */
    @Transactional
    public void deleteCartItem(Long userId, Long itemId) {
        cartMapper.deleteCartItem(userId, itemId);
    }
}