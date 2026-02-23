package com.anime.controller;

import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.anime.domain.ReviewForm;
import com.anime.mapper.RatingMapper;
import com.anime.service.MyListService;
import com.anime.service.ReviewService;
import com.anime.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final RatingMapper ratingMapper;
    private final MyListService myListService;

    

    private boolean isAnon(Authentication auth) {
        return auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName());
    }
    private long requireUserId(Authentication auth) { return userService.getUserId(auth); }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority()).collect(Collectors.toSet())
                .contains("ROLE_ADMIN");
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(value = "malId", required = false) Integer malId,
                          @RequestParam(value = "lang", defaultValue = "ko") String lang,
                          Authentication auth, Model model) {
        if (isAnon(auth)) return "redirect:/login";

        if (malId == null) {
            return "redirect:/home"; // 또는 /search 로 보내도 됨
        }

        ReviewForm form = new ReviewForm();
        form.setMalAnimeId(malId);
        model.addAttribute("form", form);
        model.addAttribute("mode", "create");
        model.addAttribute("lang", lang);
        return "reviews/reviews";
    }


    @PostMapping("/new")
    public String create(@ModelAttribute("form") ReviewForm form,
                         @RequestParam(value = "lang", defaultValue = "ko") String lang,
                         Authentication auth) {
        if (isAnon(auth)) return "redirect:/login";
        reviewService.create(requireUserId(auth), form.getMalAnimeId(),
                form.getTitle(), form.getContent(), form.getScore());
        return "redirect:/anime/" + form.getMalAnimeId() + "?lang=" + lang;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id,
                           @RequestParam(value = "lang", defaultValue = "ko") String lang,
                           Authentication auth, Model model) {
        if (isAnon(auth)) return "redirect:/login";
        long userId = requireUserId(auth);
        var review = reviewService.get(id);
        if (review == null) return "error/404";
        if (review.getUserId() != userId && !isAdmin(auth))
            return "redirect:/anime/" + review.getMalAnimeId() + "?lang=" + lang;

        ReviewForm form = new ReviewForm();
        form.setMalAnimeId(review.getMalAnimeId());
        form.setTitle(review.getTitle());
        form.setContent(review.getContent());
        form.setScore(ratingMapper.findMyScore(userId, review.getMalAnimeId())); // ✅ 기존 평점 복원
        model.addAttribute("form", form);
        model.addAttribute("reviewId", id);
        model.addAttribute("mode", "edit");
        model.addAttribute("lang", lang);
        return "reviews/reviews";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable long id,
                       @ModelAttribute("form") ReviewForm form,
                       @RequestParam(value = "lang", defaultValue = "ko") String lang,
                       Authentication auth) {
        if (isAnon(auth)) return "redirect:/login";
        long userId = requireUserId(auth);
        var review = reviewService.get(id);
        if (review == null) return "error/404";
        if (review.getUserId() == userId)
            reviewService.updateByOwner(id, userId, form.getTitle(), form.getContent(),
                    form.getScore(), review.getMalAnimeId());
        return "redirect:/anime/" + review.getMalAnimeId() + "?lang=" + lang;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id,
                         @RequestParam(value = "lang", defaultValue = "ko") String lang,
                         Authentication auth) {
        if (isAnon(auth)) return "redirect:/login";
        long userId = requireUserId(auth);
        var review = reviewService.get(id);
        if (review == null) return "error/404";
        if (review.getUserId() == userId) reviewService.deleteByOwner(id, userId);
        else if (isAdmin(auth)) reviewService.deleteByAdmin(id);
        return "redirect:/anime/" + review.getMalAnimeId() + "?lang=" + lang;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id,
                         @RequestParam(value = "lang", defaultValue = "ko") String lang,
                         @RequestParam(value = "reported", required = false) Boolean reported,
                         Authentication auth, Model model) {
        var review = reviewService.getDetail(id);
        if (review == null) return "error/404";

        boolean canEdit = !isAnon(auth) &&
                (review.getUserId() == requireUserId(auth) || isAdmin(auth));

        // ✅ 대형 카드용 애니 데이터
        var anime = myListService.getAnimeCachedCopy(review.getMalAnimeId());

        model.addAttribute("review", review);
        model.addAttribute("anime", anime);

        model.addAttribute("lang", lang);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("reported", Boolean.TRUE.equals(reported));

        return "reviews/detail";
    }



    // ─────────────────────────────────────────
    // ✅ POST /reviews/{id}/report  신고
    // ─────────────────────────────────────────
    @PostMapping("/{id}/report")
    public String report(@PathVariable long id,
                         @RequestParam(value = "lang", defaultValue = "ko") String lang,
                         Authentication auth) {
        if (isAnon(auth)) return "redirect:/login";
        reviewService.report(id);
        return "redirect:/reviews/" + id + "?lang=" + lang + "&reported=true";
    }
}