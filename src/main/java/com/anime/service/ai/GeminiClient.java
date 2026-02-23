package com.anime.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestTemplate restTemplate;

    @Value("${gemini.apiKey}")
    private String apiKey;

    private static final 
    		String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=";

    public String generate(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        if (response.getBody() == null) return null;

        // 응답 구조 파싱
        try {
            var candidates = (List<Map>) response.getBody().get("candidates");
            var content = (Map) candidates.get(0).get("content");
            var parts = (List<Map>) content.get("parts");
            return parts.get(0).get("text").toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    
    
    public String chatbot(String message) {
        System.out.println("API KEY = " + apiKey);
         String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);

         String systemPrompt = """
               너는 일본 애니메이션 정보 플랫폼 AI다.
               
               - 사용자가 질문하는 애니관련된 주제만 답변하라 .
               - 한국어로 질문하면 한국어로 답하라.
               - 일본어로 질문하면 일본어로 답하라.
               - 반드시 JSON 형식으로만 답하라.
               - 다른 설명은 절대 하지 말 것.
               
               {
	              "answer": "문장으로 답변",
				  "recommendations": ["애니1", "애니2", "애니3"],
				  "source": "anime_context|general"
               }
               """;

         Map<String, Object> body = Map.of(
                 "contents", new Object[]{
                         Map.of("parts", new Object[]{
                                 Map.of("text", systemPrompt)
                         }),
                         Map.of("parts", new Object[]{
                                 Map.of("text", message)
                         })
                 }
         );

         HttpEntity<Map<String, Object>> request =
                 new HttpEntity<>(body, headers);

         ResponseEntity<Map> response =
                 restTemplate.postForEntity(url, request, Map.class);

         try {
             Map candidate = (Map) ((java.util.List) response.getBody().get("candidates")).get(0);
             Map content = (Map) candidate.get("content");
             java.util.List parts = (java.util.List) content.get("parts");
             Map part = (Map) parts.get(0);
             return part.get("text").toString();
         } catch (Exception e) {
            e.printStackTrace();
             return "AI 응답 처리 실패";
         }
     }
    
}
