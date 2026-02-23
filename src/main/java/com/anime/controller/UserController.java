package com.anime.controller;

import com.anime.domain.ProfileForm;
import com.anime.domain.User;
import com.anime.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────
    // GET /user/profile  회원정보 페이지
    // ─────────────────────────────────────────
    @GetMapping("/profile")
    public String profilePage(Authentication auth, Model model) {
        long userId = userService.getUserId(auth);
        User user = userService.findByUsername(auth.getName());

        ProfileForm form = new ProfileForm();
        form.setNickname(user.getNickname());
        form.setEmail(user.getEmail());

        model.addAttribute("form", form);
        model.addAttribute("user", user);
        return "user/profile";
    }

    // ─────────────────────────────────────────
    // POST /user/profile  회원정보 수정
    // ─────────────────────────────────────────
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("form") ProfileForm form,
                                Authentication auth, Model model) {
        long userId = userService.getUserId(auth);
        String error = userService.updateProfile(userId, form);

        if (error != null) {
            model.addAttribute("errorMsg", error);
            model.addAttribute("user", userService.findByUsername(auth.getName()));
            return "user/profile";
        }
        return "redirect:/user/profile?updated=true";
    }

    // ─────────────────────────────────────────
    // POST /user/withdraw  회원 탈퇴
    // ─────────────────────────────────────────
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("password") String password,
                           Authentication auth,
                           HttpServletRequest request) {
        long userId = userService.getUserId(auth);
        boolean success = userService.withdraw(userId, password);

        if (!success) {
            return "redirect:/user/profile?withdrawError=true";
        }

        // 탈퇴 성공 → 세션 무효화 후 로그인 페이지로
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        return "redirect:/login?withdrawn=true";
    }
}