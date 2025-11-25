package com.example.myFarm;

import com.example.myFarm.uorder.OrderMapper;
import com.example.myFarm.command.OrderVO; // 🚨 실제 VO/DTO 경로로 변경해야 합니다.
import com.example.myFarm.util.Criteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;
// import java.util.HashMap;
// import java.util.Map;


@SpringBootTest
public class dummy_test {

    // MyBatis Mapper를 주입받습니다.
    @Autowired
    private OrderMapper orderMapper;
    private static final int TEST_USER_ID = 2;

    @Rollback(false)
    @Transactional
    @DisplayName("MyBatis를 사용한 주문 더미 데이터 100개 삽입 테스트 (랜덤 일자 포함)")
    @Test
    void insert_100_dummy_orders() {

        // ⭐ 1. 랜덤 날짜 범위 설정 (2024-11-01 ~ 2025-11-30)
        // 2025년 11월은 30일까지 있으므로 31일은 30일로 설정합니다.
        LocalDateTime startDateTime = LocalDateTime.of(2024, 11, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 11, 30, 23, 59, 59);

        // 시작일과 종료일의 에포크 초 (Epoch Seconds) 값 계산
        long minDay = startDateTime.toEpochSecond(java.time.ZoneOffset.UTC);
        long maxDay = endDateTime.toEpochSecond(java.time.ZoneOffset.UTC);

        // 기존 데이터 개수를 확인합니다.
        Criteria dummyCriteria = new Criteria();
        int initialCount = orderMapper.getTotalOrderCount(TEST_USER_ID, dummyCriteria);

        // 100개의 더미 데이터 생성 및 삽입
        for (int i = 1; i <= 100; i++) {
            OrderVO order = new OrderVO();

            // 4. 상태 (다양하게 분배)
            String status;
            if (i % 10 == 0) {
                status = "주문 대기";
            } else if (i % 5 == 0) {
                status = "배송 중";
            } else if (i % 3 == 0) {
                status = "주문 취소";
            } else {
                status = "결제 완료";
            }
            order.setStatus(status);

            // 배송지, 전화번호, 수령인
            order.setAddress("서울시 테스트구 더미동 " + (i % 5) + "번지");
            order.setPhone("010-1234-" + String.format("%04d", i));
            order.setUserId(TEST_USER_ID);
            order.setOrdRecipientName("테스트 수령인 " + i);

            // 2. 추가 필드 (updateOrderSummary 쿼리를 따로 호출해야 함. 여기서는 VO에 직접 설정)
            order.setTotalAmount(10000L + (i * 1000L));
            order.setRepresentativeItemName("테스트 상품 No." + i);

            // ⭐ 5. 주문 일자 설정 (랜덤값 적용)
            long randomSecond = ThreadLocalRandom.current().nextLong(minDay, maxDay + 1);
            LocalDateTime randomOrderDate = LocalDateTime.ofEpochSecond(randomSecond, 0, java.time.ZoneOffset.UTC);

            // OrderVO에 설정
            order.setOrderDate(randomOrderDate);

            // ⭐ DB에 저장: insertOrder 쿼리 호출 (ORDER_DATE는 insertOrder에는 없지만, keyProperty로 ORDER_ID가 채워짐)
            orderMapper.insertDummyOrder(order);

            // ⭐ 주문 총액/대표 상품명 및 ORDER_DATE 업데이트 로직 추가 (updateOrderSummary 쿼리가 ORDER_DATE를 처리한다고 가정)
            orderMapper.updateOrderSummary(order);
        }

        // 삽입 후 데이터 개수를 확인합니다.
        int finalCount = orderMapper.getTotalOrderCount(TEST_USER_ID, dummyCriteria);

        System.out.println("✅ 테스트 ID (" + TEST_USER_ID + ") 데이터: " + initialCount + " -> " + finalCount);
        if (finalCount >= initialCount + 100) {
            System.out.println("✅ 더미 데이터 100개 삽입 완료!");
        } else {
            System.out.println("❌ 더미 데이터 삽입 실패. 예상 개수 미달.");
        }
    }
}