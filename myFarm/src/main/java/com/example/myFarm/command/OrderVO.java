package com.example.myFarm.command;


import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderVO {
    private List<ItemVO> orderItems;

    private Long orderId;
    private String status;
    private String address;
    private String phone;
    private int userId;

    private LocalDate orderDate;
    private int totalAmount;
    private String representativeItemName;

}