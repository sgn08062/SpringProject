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
        int result = adminCropService.addCrop(cropVO);
        redirectAttributes.addFlashAttribute("msg", result==1? "등록 성공" : "등록 실패");
        return "redirect:/admin/crops/list";
    }

//    @PostMapping("/delete/{id}")
//    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("msg", adminCropService.deleteCrop(id)==1? "삭제" : "실패");
//        return "redirect:/admin/crops/list";
//    }
//
//    @PostMapping("/enable/{id}")
//    public String enableCrop(@PathVariable("id") long id, RedirectAttributes redirectAttributes){
//        redirectAttributes.addFlashAttribute("msg", adminCropService.enableCrop(id)==1? "활성화" : "실패");
//        return "redirect:/admin/crops/list";
//    }

    @PostMapping("/disable/{id}")
    public String disable(@PathVariable long id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("msg", adminCropService.disableCrop(id)==1? "비활성화" : "실패");
        return "redirect:/admin/crops/list";
    }
}
