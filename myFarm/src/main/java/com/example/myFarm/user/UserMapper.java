package com.example.myFarm.user;

import com.example.myFarm.command.CartVO;
import lombok.Data;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    List<CartVO> getCartList(int userId);
    int insertCart(CartVO cart);
    int updateCart(CartVO cart);
    int deleteCart(int userId, int itemId);
    int clearCart(int userId);
}