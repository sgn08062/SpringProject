package com.example.myFarm.controller;
import com.example.myFarm.user.AIChatRequest;
import com.example.myFarm.user.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor

public class AiController {
    private final AIService aiService;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody AIChatRequest request) {
        String answer = aiService.getAnswer(request.getMessage());
        return Map.of("answer", answer);
    }
}
