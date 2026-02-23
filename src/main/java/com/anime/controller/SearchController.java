package com.anime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;

import com.anime.domain.AnimeDetailResponseDto;
import com.anime.domain.AnimeSearchResponseDto;
import com.anime.service.MyListService;
import com.anime.service.ReviewService;
import com.anime.service.SearchService;
import com.anime.service.UserService;
import com.anime.service.ai.AiTranslationService;

import lombok.RequiredArgsConstructor;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final AiTranslationService translationService;
    private final UserService userService;
    private final MyListService myListService;
    private final ReviewService reviewService;


    @GetMapping("/search")
    public String search(
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            @RequestParam(value = "lang", required = false) String lang,
            Model model) {

        String resolvedLang = (lang != null && !lang.isBlank())
                ? lang
                : LocaleContextHolder.getLocale().getLanguage();

        if (query.isBlank()) {
            model.addAttribute("animeList", Collections.emptyList());
            model.addAttribute("query", "");
            model.addAttribute("lang", resolvedLang);
            return "search";
        }

        AnimeSearchResponseDto response = searchService.searchAnime(query);
        model.addAttribute("animeList", response.getData());
        model.addAttribute("query", query);
        model.addAttribute("lang", resolvedLang);

        return "search";
    }

    @GetMapping("/anime/{id}")
    public String detail(@PathVariable("id") int malId,
                         @RequestParam(value = "lang", required = false) String lang,
                         Model model,
                         Authentication auth) {

        String resolvedLang = (lang != null && !lang.isBlank())
                ? lang
                : LocaleContextHolder.getLocale().getLanguage();

        AnimeDetailResponseDto detail = searchService.getAnimeDetail(malId);
        if (detail == null || detail.getData() == null) return "error/404";

        var anime = detail.getData();

        // 서버사이드 번역 유지한다면 여기
        translationService.applyTranslation(resolvedLang, anime);

        var reviews = reviewService.listByAnime(malId);

        Long me = null;
        boolean isAdmin = false;
        if (auth != null && !"anonymousUser".equals(auth.getName())) {
            me = userService.getUserId(auth);
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        }

        boolean isFavorite = me != null && myListService.isFavorite(me, malId);

        model.addAttribute("anime", anime);
        model.addAttribute("lang", resolvedLang);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("reviews", reviews);
        model.addAttribute("me", me);
        model.addAttribute("isAdmin", isAdmin);

        return "anime/detail";
    }

}