package com.anime.controller;

import com.anime.service.ai.AiTranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslationApiController {

    private final AiTranslationService translationService;

    /**
     * GET /api/translate?malId=1&lang=ko&text=원피스
     * - 검색 결과 카드 제목 비동기 번역용
     * - 캐시 우선 조회 → 없으면 Gemini 번역 → 캐시 저장
     */
    @GetMapping
    public Map<String, String> translate(
            @RequestParam int malId,
            @RequestParam String lang,
            @RequestParam String text) {

        String result = translationService.translateTitle(malId, lang, text);
        return Map.of("translated", result != null ? result : text); // 실패 시 원문 반환
    }
}