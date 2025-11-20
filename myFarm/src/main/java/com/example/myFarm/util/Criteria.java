package com.example.myFarm.util;

import lombok.Data;

@Data
public class Criteria {
    private int pageNum;   // 현재 페이지 번호 (프론트에서 page=1로 넘어옴)
    private int amount;    // 페이지당 항목 수 (프론트에서 size=10으로 넘어옴)
    private String startDate; // 주문 시작일 (yyyy-MM-dd)
    private String endDate;   // 주문 종료일 (yyyy-MM-dd)

    public Criteria() {
        this.pageNum = 1;
        this.amount = 10;
    }

    public Criteria(int pageNum, int amount) {
        this.pageNum = pageNum;
        this.amount = amount;
    }

    // DB 쿼리에서 사용할 OFFSET 계산 메서드
    public int getOffset() {
        return (this.pageNum - 1) * this.amount;
    }
}