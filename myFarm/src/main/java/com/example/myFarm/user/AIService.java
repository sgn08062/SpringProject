package com.example.myFarm.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AIService {
    private final AIMapper aiMapper;

    @Value("${myfarm.ai.key}")
    private String apiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String getAnswer(String userMessage) {
        try {
            // 1. Gemini에게 의도 파악 요청 (Prompt Engineering)
            String prompt = generatePrompt(userMessage);
            String aiResponse = callGeminiApi(prompt);

            // 2. AI 응답(JSON) 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            // Gemini 응답 구조에서 텍스트 추출
            String jsonText = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // 코드 블록(```json ... ```) 제거 로직
            jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode parsedIntent = objectMapper.readTree(jsonText);
            String intent = parsedIntent.path("intent").asText();
            String cropName = parsedIntent.path("cropName").asText();
            // 주문 통계 질문에서는 cropName이 빈 문자열("") 또는 null이 될 수 있음.

            // 3. 의도에 따른 DB 조회 및 답변 생성
            if ("NONSENSE".equals(intent)) {
                return "질문을 다시해주세요, ex) 오이의 수확기간은 언제야? 같은 질문으로 부탁드려요.";
            }

            // --- 3-A. 농작물 관련 질문 처리 ---
            if ("CHECK_STOCK".equals(intent) || "CHECK_GROWTH".equals(intent)) {
                Long cropId = aiMapper.findCropIdByName(cropName);

                if (cropId == null) {
                    return "죄송합니다. '" + cropName + "' 해당 농작물은 없습니다.";
                }

                if ("CHECK_STOCK".equals(intent)) {
                    int amount = aiMapper.findAmountByCropId(cropId);
                    return "문의하신 " + cropName + "은(는) 현재 " + amount + "개 남아있습니다.";
                } else { // CHECK_GROWTH
                    Integer growthTime = aiMapper.findGrowthTimeByCropId(cropId);
                    // growthTime이 NULL일 경우에 대한 예외 처리 추가
                    if (growthTime == null) {
                        return cropName + "의 성장 기간 정보는 현재 등록되어 있지 않습니다.";
                    }
                    return cropName + "의 수확 시기는 약 " + growthTime + "일 입니다.";
                }
            }

            // --- 3-B 옵션. 주문 통계 관련 질문 처리 (이번 달 기준) ---
            else if ("CHECK_ORDER_COUNT".equals(intent)) {
                int count = aiMapper.findMonthlyOrderCount();
                return "이번 달 총 주문 건수는 " + count + "건 입니다.";
            } else if ("CHECK_TOTAL_AMOUNT".equals(intent)) {
                int amount = aiMapper.findMonthlyTotalAmount();
                // 통화 단위 KRW/원 을 붙이는 것이 일반적입니다.
                return "이번 달 총 주문 금액은 " + String.format("%,d", amount) + "원 입니다.";
            } else if ("CHECK_CANCEL_COUNT".equals(intent)) {
                int count = aiMapper.findMonthlyCancelCount();
                return "이번 달 주문 취소 건수는 " + count + "건 입니다.";
            }

            // --- 3-C. 기타/알 수 없는 질문 처리 ---
            else {
                return "알 수 없는 질문입니다. 농작물 정보나 주문 통계 관련 질문만 답변할 수 있습니다.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 현재 AI 서버 연결이 원활하지 않습니다. 잠시 후 다시 시도해 주세요.";
        }
    }

    private String generatePrompt(String message) {
        // AI에게 역할을 부여하고 JSON으로만 답하게 강제함
        return "You are an intent classifier JSON generator. " +
                "Analyze the following user text: '" + message + "'. " +
                "Output ONLY a JSON object with two fields: 'intent' and 'cropName'. " +
                "Rules: " +
                "1. If the user asks about quantity/stock/amount, set intent to 'CHECK_STOCK'. " +
                "2. If the user asks about time/period/growth/harvest, set intent to 'CHECK_GROWTH'. " +
                "3. If the user asks about the total number of orders/transactions, set intent to 'CHECK_ORDER_COUNT'. " +
                "4. If the user asks about the total monetary value/amount/sales, set intent to 'CHECK_TOTAL_AMOUNT'. " +
                "5. If the user asks about the number of order cancellations, set intent to 'CHECK_CANCEL_COUNT'. " +
                "6. If the input is nonsense, random chars, or irrelevant, set intent to 'NONSENSE'. " +
                "7. Extract the crop name (e.g., '오이', '수박') into 'cropName'. For order/nonsense intents, set 'cropName' to null or an empty string. " +
                "8. Do NOT output any markdown, only raw JSON. Example output: {'intent': 'CHECK_STOCK', 'cropName': '수박'}";
    }

    // callGeminiApi 메서드는 변경 사항이 없으므로 그대로 유지합니다.
    private String callGeminiApi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{contentPart});

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Object[]{content});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(GEMINI_URL + apiKey, entity, String.class);
    }
}