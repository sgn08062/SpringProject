package com.example.myFarm.controller;

import com.example.myFarm.command.ShopVO;
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

    @GetMapping
    public ResponseEntity<List<ShopVO>> getItemList() {
        List<ShopVO> items = shopService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/additem")
    public ResponseEntity<Void> addItem(@RequestBody ShopVO itemVO) {
        shopService.addItem(itemVO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<Void> updateItem(@PathVariable Long itemId,
                                           @RequestBody ShopVO itemVO) {
        shopService.updateItem(itemId, itemVO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        shopService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}