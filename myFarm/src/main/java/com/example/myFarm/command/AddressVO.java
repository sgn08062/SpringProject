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
    private int addId; // ADD_ID
    private String address; //ADDRESS
    private String addName; //ADD_NAME
    private int userId; //USER_ID
}
