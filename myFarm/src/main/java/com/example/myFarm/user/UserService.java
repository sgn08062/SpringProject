package com.example.myFarm.user;

import com.example.myFarm.command.CartVO;
import java.util.List;

public interface UserService {
    List<CartVO> getCartList(int userId);
    void addCartItem(CartVO cart);
    void updateCartItem(CartVO cart);
    void deleteCartItem(int userId, int itemId);
    void clearCart(int userId);
}