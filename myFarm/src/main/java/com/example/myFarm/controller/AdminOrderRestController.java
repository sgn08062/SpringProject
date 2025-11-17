package com.example.myFarm.controller;

import com.example.myFarm.command.OrderVO;
import com.example.myFarm.order.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public  get(@PathVariable("orderId") long orderId) {
        return orderService.findById(orderId);
    }
}
