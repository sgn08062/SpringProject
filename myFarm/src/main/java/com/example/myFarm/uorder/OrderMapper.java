package com.example.myFarm.uorder;

import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.myFarm.util.Criteria;


import java.util.List;

@Mapper
public interface OrderMapper {
//* 1. ORDERS 테이블에 주문 정보 삽입 (PK인 orderId를 리턴받음)

    void insertOrder(OrderVO order);

    // ⭐ 추가: 주문 요약 정보 업데이트 (주문 총액, 대표 상품명)
    void updateOrderSummary(OrderVO order);

//* 2. 주문 상세 항목 (ORDER_AMOUNT) 삽입

    void insertOrderAmount(@Param("orderId") Long orderId, @Param("itemId") int itemId, @Param("amount") int amount, @Param("unitPrice") int unitPrice);

//* 3. 재고 차감 (INVENTORY UPDATE)

    int updateInventoryStock(@Param("itemId") int itemId, @Param("amount") int amount);

//* 4. 재고 0 시 품절 상태 UPDATE

    int updateItemStatusToSoldOut(@Param("itemId") int itemId);

//* 5. 주문 처리 전 상품 정보 조회 (단가, 재고 상태 확인)

    ItemVO getItemForOrder(@Param("itemId") int itemId);

//* 6. 주문 목록 조회

    List<OrderVO> selectOrderList(@Param("userId") int userId);

//* 7. 주문 상세 정보 조회

    OrderVO selectOrderDetail(@Param("orderId") Long orderId, @Param("userId") int userId);

//* 8. 주문 항목 목록 조회

    List<ItemVO> selectOrderItems(@Param("orderId") Long orderId);

    // 9. 주문 취소 관련
    void cancelOrder(@Param("orderId") Long orderId, @Param("userId") int userId);

    // **[추가] 재고 복구**
    void returnInventoryStock(@Param("orderId") Long orderId);

    List<OrderVO> selectOrderListWithPaging(
            @Param("userId") int userId,
            @Param("cri") Criteria cri
    );

    // ⭐ [추가]: 검색 조건에 맞는 전체 주문 건수 조회
    int getTotalOrderCount(
            @Param("userId") int userId,
            @Param("cri") Criteria cri
    );

    void insertDummyOrder(OrderVO order);

}
