package com.anime.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeSearchResponseDto {

    private List<AnimeItem> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnimeItem {

        @JsonProperty("mal_id")
        private int malId;

        private String title;

        private Double score;

        private Images images;
        
        @JsonProperty("title_japanese")
        private String titleJapanese;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Images {
        private Jpg jpg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Jpg {

        @JsonProperty("image_url")
        private String imageUrl;
    }
}

