package com.example.myFarm.util;
import lombok.Data;

@Data
public class PageVO {
    private int startPage; // 페이징 바의 시작 번호 (예: 1, 11, 21)
    private int endPage;   // 페이징 바의 끝 번호 (예: 10, 20, 30)
    private boolean prev;  // 이전 페이지로 가는 버튼 활성화 여부
    private boolean next;  // 다음 페이지로 가는 버튼 활성화 여부
    private int total;     // 전체 데이터 개수
    private Criteria cri;  // 현재 페이지 및 항목 수 정보
    private int totalPages; // 전체 페이지 수

    public PageVO(Criteria cri, int total) {
        this.cri = cri;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / cri.getAmount());

        // 1. endPage 계산 (10개 단위)
        this.endPage = (int) (Math.ceil(cri.getPageNum() / 10.0)) * 10;

        // 2. startPage 계산
        this.startPage = this.endPage - 9;

        // 3. 실제 마지막 페이지가 endPage보다 작을 경우 endPage 조정
        if (this.endPage > totalPages) {
            this.endPage = totalPages;
        }

        // 4. prev/next 활성화 여부
        this.prev = this.startPage > 1;
        this.next = this.endPage < totalPages;
    }
}