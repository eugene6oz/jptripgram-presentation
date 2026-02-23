package com.anime.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anime.service.MyListService;
import com.anime.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mylist")
public class MyListApiController {

    // ✅ UserMapper 직접 의존 제거 → UserService 로 통일
    private final UserService userService;
    private final MyListService myListService;

    @PostMapping("/favorite/toggle")
    public Map<String, Object> toggle(@RequestParam int malId, Authentication auth) {

        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return Map.of("ok", false, "reason", "UNAUTHORIZED");
        }

        // ✅ DB 조회 없이 principal 에서 바로 꺼냄
        long userId = userService.getUserId(auth);

        boolean nowFav;
        if (myListService.isFavorite(userId, malId)) {
            myListService.favoriteOff(userId, malId);
            nowFav = false;
        } else {
            myListService.favoriteOn(userId, malId);
            nowFav = true;
        }

        return Map.of("ok", true, "favorite", nowFav);
    }
}
