package com.example.myFarm.controller;

import com.example.myFarm.command.ShopVO;
import com.example.myFarm.command.StatVO;
import com.example.myFarm.shop.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // 1. 목록 조회 (API) - http://localhost:8080/admin/shop
    @GetMapping
    public ResponseEntity<List<ShopVO>> getItemList() {
        List<ShopVO> items = shopService.getAllItems();
        return ResponseEntity.ok(items);
    }

    // 2. 등록 (API)
    @PostMapping("/additem")
    public ResponseEntity<Void> addItem(@RequestBody ShopVO itemVO) {
        shopService.addItem(itemVO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 3. 수정 (API)
    @PutMapping("/item/{itemId}")
    public ResponseEntity<Void> updateItem(@PathVariable Long itemId,
                                           @RequestBody ShopVO itemVO) {
        shopService.updateItem(itemId, itemVO);
        return ResponseEntity.ok().build();
    }

    // 4. 삭제 (API)
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        shopService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // 5. 상세 조회 (API) - http://localhost:8080/admin/shop/{itemId}
    @GetMapping("/item/{itemId}")
    public ResponseEntity<ShopVO> getItemDetail(@PathVariable Long itemId) {
        ShopVO item = shopService.getItemDetail(itemId); // 새로운 서비스 호출
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 6. 통계 조회 (API) - http://localhost:8080/admin/shop/stats
    @GetMapping("/stats")
    public ResponseEntity<StatVO> getShopStats() {
        StatVO stats = shopService.getShopStatistics();
        return ResponseEntity.ok(stats);
    }
}