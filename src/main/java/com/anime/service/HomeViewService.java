package com.anime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.anime.domain.AnimeDetailResponseDto;
import com.anime.mapper.PopularMapper;
import com.anime.service.ai.AiTranslationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeViewService {

    private final SearchService searchService;
    private final PopularMapper popularMapper;
    private final AiTranslationService aiTranslationService; // ✅ 네 프로젝트에 이미 있음

    private static final List<Integer> SEASON_PICK_IDS = List.of(
        57658, 59978, 60058, 61006, 55825, 57555
    );

    private static final long CACHE_TTL_MS = 10 * 60 * 1000;
    private static final long POPULAR_CACHE_TTL_MS = 10 * 60 * 1000;

    // ✅ 언어별 캐시를 위한 CacheItem
    private static class CacheItem<T> {
        final T value;
        final long expireAtMs;
        CacheItem(T value, long expireAtMs) {
            this.value = value;
            this.expireAtMs = expireAtMs;
        }
        boolean valid() {
            return System.currentTimeMillis() < expireAtMs;
        }
    }

    // ✅ 언어별 캐시 (기존 List<Object> 유지)
    private final Map<String, CacheItem<List<Object>>> seasonCache = new ConcurrentHashMap<>();
    private final Map<String, CacheItem<List<Object>>> popularCache = new ConcurrentHashMap<>();

    // -----------------------
    // 시즌 픽 (lang 버전)
    // -----------------------
    public List<Object> getSeasonPicks(String lang) {
        long now = System.currentTimeMillis();

        CacheItem<List<Object>> cached = seasonCache.get(lang);
        if (cached != null && cached.valid()) {
            return cached.value;
        }

        List<Object> result = new ArrayList<>();

        for (int i = 0; i < SEASON_PICK_IDS.size(); i++) {
            Integer id = SEASON_PICK_IDS.get(i);

            try {
                var detail = searchService.getAnimeDetail(id);
                if (detail != null && detail.getData() != null) {
                    Object obj = detail.getData();

                    // ✅ 실제 타입: AnimeDetailResponseDto.DataItem
                    AnimeDetailResponseDto.DataItem anime = (AnimeDetailResponseDto.DataItem) obj;

                    // ✅ 메인 리스트도 번역 적용 (title + synopsis)
                    // - en이면 AiTranslationService 내부에서 return
                    aiTranslationService.applyTranslation(lang, anime);

                    result.add(anime);
                }
            } catch (Exception e) {
                System.out.println("[HOME PICK FAIL] malId=" + id + " err=" + e.getClass().getSimpleName());
            }

            if (i < SEASON_PICK_IDS.size() - 1) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        seasonCache.put(lang, new CacheItem<>(result, now + CACHE_TTL_MS));
        return result;
    }

    // ✅ 기존 코드 호환(기본 ko)
    public List<Object> getSeasonPicks() {
        return getSeasonPicks("ko");
    }

    // -----------------------
    // 인기 애니 (lang 버전)
    // -----------------------
    public List<Object> getPopularAnimeByFavorites(String lang, int limit) {
        long now = System.currentTimeMillis();

        CacheItem<List<Object>> cached = popularCache.get(lang);
        if (cached != null && cached.valid()) {
            return cached.value;
        }

        var ids = popularMapper.topAnimeIdsByFavorites(limit);

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);

            try {
                var detail = searchService.getAnimeDetail(id);
                if (detail != null && detail.getData() != null) {
                    Object obj = detail.getData();
                    AnimeDetailResponseDto.DataItem anime = (AnimeDetailResponseDto.DataItem) obj;

                    aiTranslationService.applyTranslation(lang, anime);

                    result.add(anime);
                }
            } catch (Exception e) {
                // 429/네트워크 실패 스킵
            }

            if (i < ids.size() - 1) {
                try { Thread.sleep(250); } catch (InterruptedException ignored) {}
            }
        }

        popularCache.put(lang, new CacheItem<>(result, now + POPULAR_CACHE_TTL_MS));
        return result;
    }

    // ✅ 기존 코드 호환(기존 시그니처 유지)
    public List<Object> getPopularAnimeByFavorites(int limit) {
        return getPopularAnimeByFavorites("ko", limit);
    }
}

