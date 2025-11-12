package com.example.myFarm.user;

import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {
    /** 1. ORDERS 테이블에 주문 정보 삽입 (PK인 orderId를 리턴받음) */
    void insertOrder(OrderVO order);

    /** 2. ORDER_AMOUNT 테이블에 장바구니 항목 삽입 */
    void insertOrderAmount(OrderVO order);

    int updateInventoryStock(int itemId, int amount);

    List<ItemVO> getItemsForOrder(int userId);

    List<OrderVO> selectOrderList(int userId);
}