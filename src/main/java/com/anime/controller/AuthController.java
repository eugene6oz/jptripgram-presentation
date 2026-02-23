package com.anime.controller;

import com.anime.domain.RegisterForm;
import com.anime.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "auth.login.error");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "auth.logout.success");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("form") RegisterForm form, Model model) {

        if (userService.isUsernameTaken(form.getUsername())) {
            model.addAttribute("errorMsg", "auth.register.username.taken");
            model.addAttribute("form", form);
            return "auth/register";
        }

        if (form.getPassword() == null || form.getPassword().length() < 4) {
            model.addAttribute("errorMsg", "auth.register.password.invalid");
            model.addAttribute("form", form);
            return "auth/register";
        }

        boolean success = userService.registerUser(form);

        if (success) {
            return "redirect:/login?registered=true";
        } else {
            model.addAttribute("errorMsg", "auth.register.fail");
            model.addAttribute("form", form);
            return "auth/register";
        }
    }
}

