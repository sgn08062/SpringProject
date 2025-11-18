package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAmountDTO {
    private long orderId;
    private String recipientName;
    private String orderDate;
    private String status;
    private String address;
    private String phone;
    private String totalAmount;
    private List<OrderAmountVO> orderAmountList;
}
