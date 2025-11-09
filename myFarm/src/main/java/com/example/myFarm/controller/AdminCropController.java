package com.example.myFarm.controller;

import com.example.myFarm.admin.AdminCropService;
import com.example.myFarm.command.CropVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
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

        int result = adminCropService.addCrop(cropVO);

        // API확인용
        if(result == 1){
            return "success";
        }else{
            return "fail";
        }

        // 실제 경로
        //return "redirect:/admin/crops";
    }

    @DeleteMapping("/deleteCrop/{id}")
    public String deleteCrop(@PathVariable("id") long id,  RedirectAttributes redirectAttributes){
        int result = adminCropService.deleteCrop(id);

        if(result == 1){
            redirectAttributes.addFlashAttribute("message", "Delete Crop successfully");
        }else{
            redirectAttributes.addFlashAttribute("message", "Delete Crop failed");
        }

        return "redirect:/admin/crops";
    }

    @PostMapping("/enable/{id}")
    public String enableCrop(@PathVariable("id") long id, RedirectAttributes redirectAttributes){
        int result = adminCropService.enableCrop(id);

        if(result==1){
            return "success";
        }else {
            return "fail";
        }
    }

    @PostMapping("/disable/{id}")
    public String disableCrop(@PathVariable("id") long id, RedirectAttributes redirectAttributes){
        int result = adminCropService.disableCrop(id);

        if(result==1){
            return "success";
        }else {
            return "fail";
        }
    }
}
