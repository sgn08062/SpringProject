package com.example.myFarm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//임시 컨트롤러
@Controller
public class CommonController {

    @GetMapping("/common/main")
    public String tempMain() {
        return "common/main";
    }
}
