package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//더미데이터
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AddressVO {
    private Long addId;
    private String addName;
    private String address;
    //private String phone;
    private int userId;
}