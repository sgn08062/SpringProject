package com.example.myFarm.controller;

import com.example.myFarm.command.StatsVO;
import com.example.myFarm.stats.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsRestController {
    @Autowired
    private StatsService statsService;

    // 총 매출, 총 주문, 평균 주문액
    @GetMapping("/total")
    public ResponseEntity<StatsVO> totalStats() {
        StatsVO vo = statsService.getTotalSales();
        if(vo == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(vo);
        }
    }
}
