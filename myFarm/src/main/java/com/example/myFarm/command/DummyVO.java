package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DummyVO {

    // 1. User 필드 (유지)
    private Long userId;
    private String loginId;
    private String userPw;
    private String userName;
    private String phone;
    private String email;
    private String auth;

    // 2. Crop/Inventory 필드 (유지)
    private String uuid;
    private String cropName;
    private Integer growthTime;
    private Integer quantity;
    private String unitName;
    private Integer status;
    private Integer elapsedTick;

    private Long storId; // Inventory ID (유지)

    // 3. Product 필드 (유지)
    private Long prodId;
    private String prodEnddate;
    private String prodCategory;
    private String prodWriter;
    private String prodName;
    private Integer prodPrice;
    private Integer prodCount;
    private Integer prodDiscount;
    private String prodPurchaseYn;
    private String prodContent;
    private String prodComment;
    private Timestamp regDate;

    // 4. VO 객체 필드 (유지)
    private ItemVO item;
    private AddressVO address;
    private List<ItemVO> itemList;
    private List<AddressVO> addressList;
}