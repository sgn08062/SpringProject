package com.example.myFarm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommonController {
    @GetMapping("/common/main")
    public String main() {
        return "common/main";
    }
}
