package com.example.myFarm.controller;

import com.example.myFarm.command.ShopVO;
import com.example.myFarm.image.ImageService;
import com.example.myFarm.shop.AdminShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/shop")
public class AdminShopController {

    @Autowired
    private AdminShopService AdminshopService;

    @Autowired
    private ImageService imageService;

    // 1. 목록 조회 (API)
    @GetMapping
    public ResponseEntity<List<ShopVO>> getItemList() {
        List<ShopVO> items = AdminshopService.getAllItems();
        return ResponseEntity.ok(items);
    }

    // 2. 등록 (API)
    @PostMapping("/additem")
    @Transactional
    public ResponseEntity<Void> addItem(@ModelAttribute ShopVO shopVO,
                                        @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                                        @RequestParam(value = "detailImages", required = false) List<MultipartFile> detailImages) {
        AdminshopService.addItem(shopVO);
        System.out.println("shopVO 디버그 " + shopVO.toString());
        Long itemId = shopVO.getItemId();
        if (itemId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            imageService.saveItemImages(itemId, mainImage, detailImages);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    // 3. 수정 (API)
    @PutMapping("/item/{itemId}")
    public ResponseEntity<Void> updateItem(@PathVariable Long itemId,
                                           @RequestBody ShopVO itemVO) {
        AdminshopService.updateItem(itemId, itemVO);
        return ResponseEntity.ok().build();
    }

//    // 4. 삭제 (API)
//    @DeleteMapping("/item/{itemId}")
//    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
//        System.out.println("💥 deleteItem 호출됨: " + itemId);
//        AdminshopService.deleteItem(itemId);
//        return ResponseEntity.noContent().build();
//    }

    // 5. 상세 조회 (API)
    @GetMapping("/item/{itemId}")
    public ResponseEntity<ShopVO> getItemDetail(@PathVariable Long itemId) {
        ShopVO item = AdminshopService.getItemDetail(itemId);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 6. 판매 상태 토글 (STATUS UPDATE) API 추가
    @PutMapping("/status/{itemId}")
    public ResponseEntity<Void> updateItemStatus(@PathVariable Long itemId,
                                                 @RequestBody ShopVO shopVO) {
        AdminshopService.updateStatus(itemId, shopVO.getStatus());
        return ResponseEntity.ok().build();
    }

}