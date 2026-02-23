package com.anime.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.anime.domain.AnimeDetailResponseDto;
import com.anime.service.MyListService;
import com.anime.service.ReviewService;
import com.anime.service.UserService;
import com.anime.service.ai.AiTranslationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyListController {

    private final UserService userService;
    private final MyListService myListService;
    private final ReviewService reviewService;
    private final AiTranslationService translationService;

    @GetMapping("/mylist")
    public String mylist(
            @RequestParam(value = "lang", required = false) String lang,
            Authentication auth,
            Model model) {

        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        String resolvedLang = (lang != null && !lang.isBlank())
                ? lang
                : LocaleContextHolder.getLocale().getLanguage(); // ✅ WebConfig 기본 JA 따라감

        long userId = userService.getUserId(auth);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        model.addAttribute("lang", resolvedLang);

        // 즐겨찾기 카드
        List<Integer> favIds = myListService.findFavoriteIds(userId, 12);
        List<AnimeDetailResponseDto.DataItem> favoriteCards = new ArrayList<>();

        for (int i = 0; i < favIds.size(); i++) {
            int malId = favIds.get(i);

            AnimeDetailResponseDto.DataItem anime = myListService.getAnimeCachedCopy(malId);
            if (anime != null) {
                translationService.applyTranslation(resolvedLang, anime);
                favoriteCards.add(anime);
            }

            if (i < favIds.size() - 1) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }

        // 내가 쓴 리뷰
        var myReviews = reviewService.listByUser(userId);
        int limit = Math.min(myReviews.size(), 5);
        if (myReviews.size() > limit) myReviews = myReviews.subList(0, limit);

        model.addAttribute("favoriteCards", favoriteCards);
        model.addAttribute("myReviews", myReviews);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("isAdmin", isAdmin);

        return "anime/mylist";
    }
}


