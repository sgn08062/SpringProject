package com.example.myFarm.command;

import lombok.*;

import java.util.List; // List 임포트 추가

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO {
    private Long itemId;
    private String itemName;
    private Integer price;
    private Integer status;
    private Long storId;
    private Integer inventoryAmount;
    private String description;

    // ⭐️ ImageService에서 조회한 이미지 목록을 담기 위해 추가
    private List<ImageVO> images;
}