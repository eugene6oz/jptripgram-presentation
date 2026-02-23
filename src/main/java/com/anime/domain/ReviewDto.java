package com.anime.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private Long userId;
    private Integer malAnimeId;
    private Integer score;

    private String title;
    private String content;
    private Integer reportCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 화면용 (조인 결과)
    private String username;
    private String nickname;
}
