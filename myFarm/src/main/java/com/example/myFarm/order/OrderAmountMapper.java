package com.example.myFarm.order;

import com.example.myFarm.command.OrderAmountDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderAmountMapper {
    List<OrderAmountDTO> findById(long orderId);
}
