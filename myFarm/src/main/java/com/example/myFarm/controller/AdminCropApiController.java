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

    // 농작물 조회
    @GetMapping("/{id}")
    public ResponseEntity<CropVO> getCropById(@PathVariable("id") long cropId){
        CropVO vo = adminCropService.getCropById(cropId);
        if(vo == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok().body(vo);
        }
    }

    // 농작물 수정
    @PostMapping("/{id}")
    public ResponseEntity<?> updateCrop(@PathVariable("id") long cropId, @RequestBody CropVO cropVO){
        cropVO.setCropId(cropId);
        if (cropVO.getGrowthTime() <= 0){
            cropVO.setGrowthTime(60); // 성장 시간이 0이하로 입력될시 디폴트값
        }
        int result = adminCropService.updateCrop(cropVO);

        if(result == 1){
            return ResponseEntity.ok().body(cropVO);
        }else{
            return ResponseEntity.badRequest().body("update failed");
        }
    }
}
