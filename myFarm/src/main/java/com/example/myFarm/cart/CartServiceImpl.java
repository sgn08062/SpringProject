
package com.example.myFarm.cart;

import com.example.myFarm.command.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;

    @Override
    public List<CartVO> getCartList(int userId) {
        return cartMapper.getCartList(userId);
    }

    @Override
    @Transactional
    public void addCartItem(CartVO cart) {
        // 장바구니에 이미 상품이 있는지 확인 후 update 또는 insert 로직 추가 가능
        // 현재는 단순 insert
        cartMapper.insertCart(cart);
    }

    @Override
    @Transactional
    public void updateCartItem(CartVO cart) {
        cartMapper.updateCart(cart);
    }

    @Override
    @Transactional
    public void deleteCartItem(int userId, int itemId) {
        cartMapper.deleteCart(userId, itemId);
    }

    @Override
    @Transactional
    public void clearCart(int userId) {
        cartMapper.clearCart(userId);
    }
}
