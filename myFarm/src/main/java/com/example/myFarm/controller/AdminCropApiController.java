package com.example.myFarm.controller;

import com.example.myFarm.admin.AdminCropService;
import com.example.myFarm.command.CropVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/crops")
public class AdminCropApiController {

    @Autowired
    private AdminCropService adminCropService;

    // 재배 농작물 목록
    @GetMapping
    public List<CropVO> list(){
        return adminCropService.getCropList();
    }

    // 새 농작물 등록
    @PostMapping
    public ResponseEntity<CropVO> create(@RequestBody CropVO cropVO){
        if(cropVO.getGrowthTime() <= 0) cropVO.setGrowthTime(60); // 기본 성장 틱
        cropVO.setElapsedTick(0);

        int r = adminCropService.addCrop(cropVO);
        if(r == 1){
            return ResponseEntity.ok().body(cropVO);
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

    // 농작물 활성화
    @PostMapping("/enable/{cropid}")
    public ResponseEntity<?> enable(@PathVariable("cropid") long cropId){
        int r = adminCropService.enableCrop(cropId);
        if(r==1){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.badRequest().body("enable failed");
        }
    }

    // 농작물 비활성
    @PostMapping("/disable/{cropid}")
    public ResponseEntity<?> disable(@PathVariable("cropid") long cropId){
        int r = adminCropService.disableCrop(cropId);
        if(r==1){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.badRequest().body("disable failed");
        }
    }
}
