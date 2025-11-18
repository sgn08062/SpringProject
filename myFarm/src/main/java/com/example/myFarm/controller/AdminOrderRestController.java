package com.example.myFarm.controller;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.order.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/order")
public class AdminOrderRestController {
    @Autowired
    AdminOrderService orderService;

    @GetMapping("/list")
    public List<OrderVO> list() {
        return orderService.list();
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
        } else {
            return ResponseEntity.status(404).body("order not found");
        }
    }
}
