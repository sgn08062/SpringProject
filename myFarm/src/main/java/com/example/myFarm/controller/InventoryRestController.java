package com.example.myFarm.controller;

import com.example.myFarm.command.InventoryVO;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/inventory")
public class InventoryRestController {

    @Autowired
    private InventoryService inventoryService;

    // 인벤토리 리스트 조회
    @GetMapping
    public ResponseEntity<List<InventoryVO>> getInventoryItems() {
        List<InventoryVO> items = inventoryService.getAll();
        return ResponseEntity.ok(items);
    }

    // 특정 아이템 조회
    @GetMapping("/{id}")
    public ResponseEntity<InventoryVO> detail(@PathVariable("id") long storId) {
        InventoryVO vo = inventoryService.getByStorId(storId);
        if(vo == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok().body(vo);
        }
    }

    // 농작물 생성과 동시에 인벤토리에 자리 생성
//    @PostMapping("/init/{cropId}")
//    public String init(@PathVariable long cropId, RedirectAttributes ra) {
//        inventoryService.initForCrop(cropId);
//        ra.addFlashAttribute("msg", "재고가 초기화되었습니다.");
//        return "redirect:/admin/inventory/" + cropId;
//    }
}
