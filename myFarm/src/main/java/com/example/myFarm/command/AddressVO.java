package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressVO {
    private int addressId; // ADD_ID
    private int userId; //USER_ID
    private String address; //ADDRESS
    private String addressName; //ADDRESS_NAME
    private String recipientName; //RECIPIENT_NAME
    private String recipientPhone; //RECIPIENT_PHONE
}
