package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private long orderId;
    private String orderDate;
    private String status;
    private String address;
    private String phone;
    private long totalAmount;
    private String repItemName;
    private long userId;
}
