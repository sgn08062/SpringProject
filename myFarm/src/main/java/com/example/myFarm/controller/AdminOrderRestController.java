package com.example.myFarm.controller;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.order.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/order")
public class AdminOrderRestController {
    @Autowired
    AdminOrderService orderService;

    @GetMapping("/list")
    public List<OrderVO> list(
            @RequestParam(required = false) String customerName, // 고객명 검색 파라미터 추가
            @RequestParam(required = false) String status        // 상태 필터링 파라미터 추가
    ) {

        // 검색 조건을 담을 Map 생성 및 값 할당
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("customerName", customerName);
        searchConditions.put("status", status);

        // Service 계층에 검색 조건을 전달하는 새로운 메소드를 호출
        return orderService.getFilteredOrderList(searchConditions);

    }

    // 주문 조회 ( 상세보기 선택 시 )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderAmountDTO> get(@PathVariable("orderId") long orderId) {
        OrderAmountDTO dto = orderService.findById(orderId);
        return ResponseEntity.ok(dto);
    }

    // 주문 상태 업데이트
    @PostMapping("/{orderId}/status")
    public ResponseEntity<String> updateStatus(@PathVariable("orderId") long orderId,
                                               @RequestBody Map<String, String> body) {
        String status = body.get("status");

        if (status == null ||
                !(status.equals("주문 대기")
                        || status.equals("배송 중")
                        || status.equals("결제 완료")
                        || status.equals("주문 취소"))) {
            return ResponseEntity.badRequest().body("invalid status");
        }

        int result = orderService.updateStatus(orderId, status);
        if (result == 1) {
            return ResponseEntity.ok("success");
        } else if (result == -1){
            return ResponseEntity.badRequest().body("already cancel");
        }
        else {
            return ResponseEntity.status(404).body("order not found");
        }
    }
}
