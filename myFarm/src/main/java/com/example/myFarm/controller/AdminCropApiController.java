package com.example.myFarm.controller;

import com.example.myFarm.admin.AdminCropService;
import com.example.myFarm.command.CropVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/crops")
public class AdminCropApiController {

    @Autowired
    private AdminCropService adminCropService;

    // 재배 농작물 목록
    @GetMapping
    public List<CropVO> list(){
        return adminCropService.getCropList();
    }
}
