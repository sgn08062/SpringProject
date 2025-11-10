package com.example.myFarm.controller;

import com.example.myFarm.admin.AdminCropService;
import com.example.myFarm.command.CropVO;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/crops")
public class AdminCropController {
    @Autowired
    private AdminCropService adminCropService;

    @GetMapping("/list")
    public String getList(Model model) {

        return "/admin/crops/list";
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
        //return "redirect:/admin/crops/list";
    }

    @PostMapping("/deleteCrop/{uuid}")
    public String deleteCrop(@PathVariable("uuid") String uuid,  RedirectAttributes redirectAttributes){
        int result = adminCropService.deleteCrop(uuid);

        if(result == 1){
            redirectAttributes.addFlashAttribute("message", "Delete Crop successfully");
        }else{
            redirectAttributes.addFlashAttribute("message", "Delete Crop failed");
        }

        return "redirect:/admin/crops/list";
    }

    @PostMapping("/enable/{uuid}")
    public String enableCrop(@PathVariable("uuid") String uuid, RedirectAttributes redirectAttributes){
        int result = adminCropService.enableCrop(uuid);

        if(result==1){
            return "success";
        }else {
            return "fail";
        }
    }

    @PostMapping("/disable/{uuid}")
    public String disableCrop(@PathVariable("uuid") String uuid, RedirectAttributes redirectAttributes){
        int result = adminCropService.disableCrop(uuid);

        if(result==1){
            return "success";
        }else {
            return "fail";
        }
    }
}
