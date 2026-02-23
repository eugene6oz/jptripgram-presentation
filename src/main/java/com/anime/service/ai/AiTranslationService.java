package com.anime.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.anime.domain.AnimeDetailResponseDto;
import com.anime.mapper.TranslationCacheMapper;

@Service
@RequiredArgsConstructor
public class AiTranslationService {

    private final GeminiClient geminiClient;
    private final TranslationCacheMapper cacheMapper;

    // ─────────────────────────────────────────
    // 상세 페이지용 (title + synopsis 동기 번역)
    // ─────────────────────────────────────────
    public void applyTranslation(String lang, AnimeDetailResponseDto.DataItem anime) {
        if (lang == null || lang.isBlank() || "en".equals(lang)) return;

        int malId = anime.getMalId();
        String cachedTitle    = cacheMapper.findTitle(malId, lang);
        String cachedSynopsis = cacheMapper.findSynopsis(malId, lang);

        if (cachedTitle != null && cachedSynopsis != null) {
            anime.setTitle(cachedTitle);
            anime.setSynopsis(cachedSynopsis);
            return;
        }

        String translatedTitle    = translateSafe(anime.getTitle(), lang);
        String translatedSynopsis = translateSafe(anime.getSynopsis(), lang);
        if (translatedTitle == null || translatedSynopsis == null) return;

        cacheMapper.upsert(malId, lang, translatedTitle, translatedSynopsis);
        anime.setTitle(translatedTitle);
        anime.setSynopsis(translatedSynopsis);
    }

    // ─────────────────────────────────────────
    // ✅ 검색 결과 카드 비동기 번역용
    //    TranslationApiController → JS fetch 에서 호출
    //    캐시 우선 → 없으면 Gemini → 캐시 저장
    // ─────────────────────────────────────────
    public String translateTitle(int malId, String lang, String text) {
        if (lang == null || lang.isBlank() || "en".equals(lang)) return text;

        // 캐시 확인
        String cached = cacheMapper.findTitle(malId, lang);
        if (cached != null) return cached;

        // Gemini 번역
        String translated = translateSafe(text, lang);
        if (translated == null) return null;

        // 캐시 저장 (synopsis는 기존 값 유지)
        String existingSynopsis = cacheMapper.findSynopsis(malId, lang);
        cacheMapper.upsert(malId, lang, translated,
                existingSynopsis != null ? existingSynopsis : "");

        return translated;
    }

    // ─────────────────────────────────────────
    // 공통 번역 (실패 시 null 반환)
    // ─────────────────────────────────────────
    private String translateSafe(String text, String targetLang) {
        if (text == null || text.isBlank()) return text;

        String langName = switch (targetLang) {
            case "ko" -> "Korean";
            case "ja" -> "Japanese";
            default   -> "English";
        };

        String prompt = """
                Translate the following anime title to %s.
                Output ONLY the translation. Do not add explanation or romanization.

                TEXT:
                %s
                """.formatted(langName, text);

        try {
            return geminiClient.generate(prompt);
        } catch (HttpClientErrorException.TooManyRequests e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}