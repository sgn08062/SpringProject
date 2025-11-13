package com.example.myFarm.controller;

import com.example.myFarm.command.InventoryVO;
import com.example.myFarm.inventory.InventoryMapper;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("items", inventoryService.getAll());
        return "admin/inventory/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") long cropId, Model model, RedirectAttributes redirectAttributes) {
        InventoryVO vo = inventoryService.getByCropId(cropId);
        if (vo == null) {
            redirectAttributes.addFlashAttribute("msg", "해당 작물 재고가 없습니다. 먼저 초기화 해주세요.");
            return "redirect:/admin/inventory/list";
        }
        model.addAttribute("item", vo);
        return "admin/inventory/detail/" + vo.getCropId();
    }

    @PostMapping("/init/{cropId}")
    public String init(@PathVariable long cropId, RedirectAttributes ra) {
        inventoryService.initForCrop(cropId);
        ra.addFlashAttribute("msg", "재고가 초기화되었습니다.");
        return "redirect:/admin/inventory/" + cropId;
    }
}
