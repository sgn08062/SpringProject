package com.example.myFarm.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface AIMapper {
    // 1. 농작물 이름으로 작물 존재 여부 및 ID 확인
    @Select("SELECT CROP_ID FROM crop WHERE CROP_NAME = #{cropName}")
    Long findCropIdByName(String cropName);

    // 2. 재고 확인 (inventory 테이블)
    @Select("SELECT IFNULL(SUM(AMOUNT), 0) FROM inventory WHERE CROP_ID = #{cropId}")
    int findAmountByCropId(Long cropId);

    // 3. 수확 시기 확인 (crop 테이블)
    @Select("SELECT GROWTH_TIME FROM crop WHERE CROP_ID = #{cropId}")
    Integer findGrowthTimeByCropId(Long cropId);

    // 4. 월별 총 주문 건수 조회
    @Select("SELECT COUNT(ORDER_ID) FROM orders WHERE DATE_FORMAT(ORDER_DATE, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")
    int findMonthlyOrderCount();

    // 5. 월별 총 주문 금액 조회
    @Select("SELECT IFNULL(SUM(TOTAL_AMOUNT), 0) FROM orders WHERE DATE_FORMAT(ORDER_DATE, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")
    int findMonthlyTotalAmount();

    // 6. 월별 주문 취소 건수 조회 (STATUS가 '주문 취소'인 건 필터링)
    @Select("SELECT COUNT(ORDER_ID) FROM orders WHERE STATUS = '주문 취소' AND DATE_FORMAT(ORDER_DATE, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")
    int findMonthlyCancelCount();
}
