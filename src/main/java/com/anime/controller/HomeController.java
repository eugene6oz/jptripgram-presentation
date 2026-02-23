package com.anime.controller;

import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.anime.mapper.MyAnimeListMapper;
import com.anime.mapper.UserMapper;
import com.anime.service.HomeViewService;
import com.anime.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeViewService homeViewService;
    private final UserMapper UserMapper;
    private final MyAnimeListMapper myAnimeListMapper;
    private final ReviewService reviewService;

    
    
    // 대문
    @GetMapping("/")
    public String landing() {
        return "index";
    }
    @GetMapping("/home")
    public String home(@RequestParam(value = "lang", required = false) String lang,
                       Authentication auth,
                       Model model) {

        String resolvedLang = (lang != null && !lang.isBlank())
                ? lang
                : LocaleContextHolder.getLocale().getLanguage(); // ✅ WebConfig 기본 JA 따라감

        model.addAttribute("lang", resolvedLang);
        model.addAttribute("seasonPicks", homeViewService.getSeasonPicks(resolvedLang));
        model.addAttribute("popularAnime", homeViewService.getPopularAnimeByFavorites(resolvedLang, 6));
        model.addAttribute("recentReviews", reviewService.listRecent(5));

        if (auth != null && !"anonymousUser".equals(auth.getName())) {
            long userId = UserMapper.findIdByUsername(auth.getName());
            List<Integer> favoriteIds = myAnimeListMapper.findFavoriteIds(userId, 100);
            model.addAttribute("favoriteIds", favoriteIds);
        }

        return "home/main";
    }


    
}

