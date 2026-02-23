package com.anime.service;

import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.anime.domain.AnimeDetailResponseDto;
import com.anime.domain.AnimeSearchResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestTemplate restTemplate;

    public AnimeSearchResponseDto searchAnime(String query) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.jikan.moe/v4/anime")
                .queryParam("q", query)
                .queryParam("limit", 12)
                // ✅ 성인/노골적 컨텐츠 제외 (목록에서부터 차단)
                .queryParam("sfw", true)
                .build()
                .encode()
                .toUriString();

        AnimeSearchResponseDto res = restTemplate.getForObject(url, AnimeSearchResponseDto.class);
        if (res == null || res.getData() == null) {
            AnimeSearchResponseDto empty = new AnimeSearchResponseDto();
            empty.setData(Collections.emptyList());
            return empty;
        }

        // ✅ (보험) 혹시 섞여오면 한 번 더 필터링 가능
        // res.setData(res.getData().stream().filter(d -> !isAdultSearchItem(d)).collect(Collectors.toList()));

        return res;
    }

    public AnimeDetailResponseDto getAnimeDetail(int malId) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.jikan.moe/v4/anime/{id}")
                .buildAndExpand(malId)
                .encode()
                .toUriString();

        AnimeDetailResponseDto res = restTemplate.getForObject(url, AnimeDetailResponseDto.class);

        // ✅ 상세에서도 차단 (id로 직접 접근하는 경우 막기)
        if (res != null && res.getData() != null && isAdultDetail(res.getData())) {
            return null; // 컨트롤러에서 null이면 404 처리하도록
        }

        return res;
    }

    // ─────────────────────────────────────────
    // ✅ 성인 애니 판별 (상세 기준)
    // ─────────────────────────────────────────
    private boolean isAdultDetail(AnimeDetailResponseDto.DataItem data) {
        if (data == null) return false;

        // 1) rating 기준 (Rx - Hentai 등)
        if (data.getRating() != null) {
            String r = data.getRating().toLowerCase();
            if (r.contains("hentai")) return true;
            if (r.startsWith("rx")) return true;
        }

        // 2) 장르에 Hentai 포함되면 차단 (보험)
        if (data.getGenres() != null &&
            data.getGenres().stream().anyMatch(g -> g != null && g.getName() != null
                    && "hentai".equalsIgnoreCase(g.getName()))) {
            return true;
        }

        return false;
    }
}

