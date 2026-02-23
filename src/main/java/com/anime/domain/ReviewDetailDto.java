package com.anime.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewDetailDto {
    private Long id;
    private Long userId;
    private Integer malAnimeId;

    private String title;
    private String content;
    private Integer reportCount;
    
    // ✅ 작성자가 준 평점(1~10, 0.5단위). 없으면 null
    private Integer score;

    // 작성자 표시용
    private String username;
    private String nickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
