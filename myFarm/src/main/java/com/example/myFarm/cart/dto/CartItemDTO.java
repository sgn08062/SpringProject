package com.example.myFarm.cart.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private int quantity;

    // (Join해서 가져올 상품 정보 - 선택 사항)
    // private String productName;
    // private int productPrice;
}