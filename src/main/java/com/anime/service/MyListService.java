package com.anime.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anime.domain.AnimeDetailResponseDto;
import com.anime.mapper.MyAnimeListMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyListService {

    private final MyAnimeListMapper myAnimeListMapper;
    private final SearchService searchService;

    // ✅ AiTranslationService 의존 제거 → Controller에서 직접 번역 적용
    //    (캐시 오염 방지를 위해 번역은 복사본에만 적용)

    private static class CacheItem<T> {
        final T value;
        final long expireAtMs;
        CacheItem(T value, long expireAtMs) { this.value = value; this.expireAtMs = expireAtMs; }
        boolean valid() { return System.currentTimeMillis() < expireAtMs; }
    }

    private final Map<Integer, CacheItem<AnimeDetailResponseDto.DataItem>> animeCache
            = new ConcurrentHashMap<>();
    private static final long DETAIL_TTL_MS = 10 * 60 * 1000; // 10분

    public boolean isFavorite(long userId, int malId) {
        return myAnimeListMapper.isFavorite(userId, malId) == 1;
    }

    /**
     * 캐시에서 원본을 가져와 얕은 복사본 반환
     * 번역은 복사본에 적용 → 캐시 원본 오염 방지
     */
    public AnimeDetailResponseDto.DataItem getAnimeCachedCopy(int malId) {
        AnimeDetailResponseDto.DataItem original = getAnimeCached(malId);
        if (original == null) return null;

        // ✅ 새 DataItem에 필드 복사 (번역이 캐시 원본에 영향 안 주도록)
        AnimeDetailResponseDto.DataItem copy = new AnimeDetailResponseDto.DataItem();
        copy.setMalId(original.getMalId());
        copy.setTitle(original.getTitle());
        copy.setSynopsis(original.getSynopsis());
        copy.setScore(original.getScore());
        copy.setImages(original.getImages());
        copy.setTitleJapanese(original.getTitleJapanese());
        copy.setEpisodes(original.getEpisodes());
        copy.setStatus(original.getStatus());
        copy.setGenres(original.getGenres());
        copy.setStudios(original.getStudios());
        return copy;
    }

    private AnimeDetailResponseDto.DataItem getAnimeCached(int malId) {
        CacheItem<AnimeDetailResponseDto.DataItem> cached = animeCache.get(malId);
        if (cached != null && cached.valid()) return cached.value;

        try {
            var detail = searchService.getAnimeDetail(malId);
            if (detail != null && detail.getData() != null) {
                AnimeDetailResponseDto.DataItem data = detail.getData();
                animeCache.put(malId, new CacheItem<>(data, System.currentTimeMillis() + DETAIL_TTL_MS));
                return data;
            }
        } catch (Exception e) {
            // 스킵
        }
        return null;
    }

    public List<Integer> findFavoriteIds(long userId, int limit) {
        return myAnimeListMapper.findFavoriteIds(userId, limit);
    }

    @Transactional
    public void favoriteOn(long userId, int malId) {
        myAnimeListMapper.favoriteOn(userId, malId);
    }

    @Transactional
    public void favoriteOff(long userId, int malId) {
        myAnimeListMapper.favoriteOff(userId, malId);
    }
}

