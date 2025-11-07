package com.example.myFarm.controller;

import com.example.myFarm.command.CropVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/admin/crops")
public class AdminCropController {

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

        return "redirect:/admin/crops";
    }
}
