package com.example.myFarm.user;

import com.example.myFarm.command.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public List<CartVO> getCartList(int userId) {
        return userMapper.getCartList(userId);
    }

    @Override
    @Transactional
    public void addCartItem(CartVO cart) {
        userMapper.insertCart(cart);
    }

    @Override
    @Transactional
    public void updateCartItem(CartVO cart) {
        userMapper.updateCart(cart);
    }

    @Override
    @Transactional
    public void deleteCartItem(int userId, int itemId) {
        userMapper.deleteCart(userId, itemId);
    }

    @Override
    @Transactional
    public void clearCart(int userId) {
        userMapper.clearCart(userId);
    }
}