package com.example.myFarm.order;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;

import java.util.List;
import java.util.Map;

public interface AdminOrderService {
    List<OrderVO> list();
    OrderAmountDTO findById(long orderId);
    int updateStatus(long orderId, String status);
    List<OrderVO> getFilteredOrderList(Map<String, Object> conditions);
}
