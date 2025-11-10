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

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", inventoryService.getAll());
        return "admin/inventory/list";
    }

    @GetMapping("/{uuid}")
    public String detail(@PathVariable("uuid") String uuid, Model model) {
        InventoryVO vo = inventoryService.getByCropId(uuid);
        model.addAttribute("vo", vo);
        return "admin/inventory/detail";
    }

}
