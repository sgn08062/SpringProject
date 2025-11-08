package com.example.myFarm.controller;

import com.example.myFarm.admin.AdminCropService;
import com.example.myFarm.command.CropVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/admin/crops")
public class AdminCropController {
    @Autowired
    private AdminCropService adminCropService;

    @GetMapping
    public String getList(){
        return "/admin/crops";
    }

    @GetMapping("/addCrop")
    public String addCrop(){
        return "/admin/crops/addCrop";
    }

    @PostMapping("/addCrop")
    public String addCrop(CropVO cropVO, RedirectAttributes redirectAttributes){
        System.out.println(cropVO.toString());

        adminCropService.addCrop(cropVO);

        // API확인용
        return "success";

        // 실제 경로
        //return "redirect:/admin/crops";
    }

    @DeleteMapping("/deleteCrop/{id}")
    public String deleteCrop(@PathVariable int id){


        // API확인용
        return "success";
    }
}
