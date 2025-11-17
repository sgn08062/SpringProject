package com.example.myFarm.order;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {
    @Autowired
    AdminOrderMapper adminOrderMapper;
    @Autowired
    OrderAmountMapper orderAmountMapper;

    @Override
    public List<OrderVO> list() {
        return adminOrderMapper.list();
    }

    @Override
    public List<OrderAmountDTO> findById(long orderId) {
        return orderAmountMapper.findById(orderId);
    }
}
