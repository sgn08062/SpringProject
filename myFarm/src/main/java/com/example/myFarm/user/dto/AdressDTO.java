package com.example.myFarm.user.dto;

import lombok.Data;

/**
 * [v12] DB의 'ADDRESS' 테이블과 매핑되는 DTO
 */
@Data
public class AdressDTO {
    private Long addId;     // from ADD_ID
    private String address; // from ADDRESS
    private String addName; // from ADD_NAME
    private Long userId;    // from USER_ID (FK)
}