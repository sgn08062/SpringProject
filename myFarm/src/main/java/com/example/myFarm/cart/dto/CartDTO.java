/*
package com.example.myFarm.cart.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

*/
/**
 * [v12] DB의 'CART' 테이블과 1:1로 매핑되는 DTO
 *//*

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    // DB의 INT/BIGINT는 Java에서 Long으로 받는 것이 안전합니다.
    private Long userId;  // from USER_ID (복합키 1)
    private Long itemId;  // from ITEM_ID (복합키 2, SHOP의 FK)
    private int amount;   // from AMOUNT
}*/
