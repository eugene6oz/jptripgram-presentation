package com.anime.domain;

import lombok.Data;

@Data
public class ReviewForm {
    private Integer malAnimeId;
    private String title;
    private String content;
    private Integer score; // 1~10, null 허용(평점 안 줄 수도)
}
