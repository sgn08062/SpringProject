package com.example.myFarm.controller;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.order.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
