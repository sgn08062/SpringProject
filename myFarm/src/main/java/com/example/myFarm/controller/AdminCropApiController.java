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

    @PostMapping("/enable/{id}")
    public ResponseEntity<?> enable(@PathVariable("id") long cropId){
        int r = adminCropService.enableCrop(cropId);
        if(r==1){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.badRequest().body("enable failed");
        }
    }

    @PostMapping("/disable/{id}")
    public ResponseEntity<?> disable(@PathVariable("id") long cropId){
        int r = adminCropService.disableCrop(cropId);
        if(r==1){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.badRequest().body("disable failed");
        }
    }
}
