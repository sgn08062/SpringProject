/*
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

    */
/**
     * [중요] MemberMapper 없이 ID를 찾는 방법
     * CartMapper에 findUserIdByLoginId 라는 쿼리를 하나 추가해서 해결합니다.
     *//*

    private Long getUserId(String loginId) {
        Long userId = cartMapper.findUserIdByLoginId(loginId);
        if (userId == null) {
            throw new IllegalStateException("존재하지 않는 회원입니다: " + loginId);
        }
        return userId;
    }

    public List<CartViewDTO> getCartItems(String loginId) {
        Long userId = getUserId(loginId); // 여기서 변환
        return cartMapper.findCartItemsByUserId(userId);
    }

    @Transactional
    public void addItemToCart(String loginId, Long itemId, int quantity) {
        Long userId = getUserId(loginId); // 여기서 변환

        Optional<CartDTO> existingItem = cartMapper.findCartItemByCompositeKey(userId, itemId);

        if (existingItem.isPresent()) {
            CartDTO item = existingItem.get();
            int newAmount = item.getAmount() + quantity;
            if (newAmount <= 0) {
                cartMapper.deleteCartItem(userId, itemId);
            } else {
                item.setAmount(newAmount);
                cartMapper.updateCartItemAmount(item);
            }
        } else {
            if (quantity > 0) {
                CartDTO newItem = new CartDTO(userId, itemId, quantity);
                cartMapper.insertCartItem(newItem);
            }
        }
    }

    @Transactional
    public void deleteCartItem(String loginId, Long itemId) {
        Long userId = getUserId(loginId); // 여기서 변환
        cartMapper.deleteCartItem(userId, itemId);
    }
}*/
