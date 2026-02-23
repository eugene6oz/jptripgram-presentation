package com.anime.service.ai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.anime.domain.ChatRequestDto;
import com.anime.domain.ChatResponse;
import com.anime.service.SearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiClient geminiClient;
    private final SearchService searchService;
    private final ObjectMapper objectMapper; // ✅ 주입받기 (추천)

    public ChatResponse ask(ChatRequestDto req) {

        var detail = searchService.getAnimeDetail(req.getMalId());
        if (detail == null || detail.getData() == null) {
            ChatResponse r = new ChatResponse("Anime not found", List.of());
            r.setSource("general");
            return r;
        }

        var anime = detail.getData();

        String lang = (req.getLang() == null) ? "ko" : req.getLang().trim();

        String languageRule = switch (lang) {
            case "ja" -> "Answer in Japanese.";
            case "en" -> "Answer in English.";
            default -> "Answer in Korean.";
        };

        String prompt = """
                You are an anime assistant for an anime information site.
                %s

                Anime title: %s
                Synopsis: %s

                User question: %s

                IMPORTANT:
                - Return ONLY valid JSON.
                - No markdown.
                - No explanation outside JSON.

                JSON format:
                {
                  "answer": "string",
                  "recommendations": ["string","string","string"]
                }
                """.formatted(
                languageRule,
                safe(anime.getTitle()),
                safe(anime.getSynopsis()),
                safe(req.getQuestion())
        );

        String raw = geminiClient.generate(prompt);
        if (raw == null) raw = "";

        try {
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);

            String answer = node.path("answer").asText("");
            List<String> recs = new ArrayList<>();

            JsonNode arr = node.path("recommendations");
            if (arr.isArray()) {
                for (JsonNode it : arr) recs.add(it.asText());
            }

            ChatResponse r = new ChatResponse(answer, recs);
            r.setSource("anime_context");
            return r;

        } catch (Exception e) {
            ChatResponse r = new ChatResponse("죄송합니다. 답변 생성에 실패했습니다.", List.of());
            r.setSource("anime_context");
            return r;
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    // ✅ 모델이 앞뒤에 텍스트 섞어도 { ... }만 뽑아오기
    private String extractJson(String raw) {
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start < 0 || end < 0 || end <= start) {
            // JSON이 아니면 기본 JSON 반환 (파싱 실패 방지)
            return "{\"answer\":\"\",\"recommendations\":[]}";
        }
        return raw.substring(start, end + 1);
    }
}


