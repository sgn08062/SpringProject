package com.example.myFarm.cart.dto;

import lombok.Data;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class CartDTO {
    private Long cartId;
    private Long memberId;
}