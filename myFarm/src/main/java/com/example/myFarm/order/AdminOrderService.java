package com.example.myFarm.order;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;

import java.util.List;

public interface AdminOrderService {
    List<OrderVO> list();
    OrderAmountDTO findById(long orderId);
}
