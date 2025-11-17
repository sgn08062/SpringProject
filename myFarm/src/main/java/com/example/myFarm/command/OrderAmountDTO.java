package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAmountDTO {
    long orderId;
    long itemId;
    long amount;
    long unitPrice;
}
