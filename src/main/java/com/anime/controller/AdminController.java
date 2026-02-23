package com.anime.controller;

import com.anime.domain.ReviewDto;
import com.anime.service.ReviewService;
import com.anime.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping
    public String adminMain(Model model) {
        List<ReviewDto> reviews = reviewService.listAll();

        // ✅ 람다 대신 Java 8 호환 방식으로 신고 건수 카운트
        int reportedCount = 0;
        for (ReviewDto r : reviews) {
            if (r.getReportCount() != null && r.getReportCount() > 0) {
                reportedCount++;
            }
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("reportedCount", reportedCount); // ✅ 모델로 전달
        return "admin/dashboard";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable long id) {
        reviewService.deleteByAdmin(id);
        return "redirect:/admin";
    }

    @PostMapping("/reviews/{id}/report-reset")
    public String resetReport(@PathVariable long id) {
        reviewService.resetReportCount(id);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
        return "redirect:/admin";
    }
}