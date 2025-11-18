package com.example.myFarm.order;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminOrderMapper {
    List<OrderVO> list();
    int updateStatus(long orderId, String status);

    String getStatus(long orderId);
}
