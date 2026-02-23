package com.anime.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeDetailResponseDto {

    private DataItem data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataItem {
        @JsonProperty("mal_id")
        private int malId;
        
        @JsonProperty("rating")
        private String rating;

        private String title;

        @JsonProperty("title_japanese")
        private String titleJapanese;

        private String synopsis;

        private Double score;

        private Integer episodes;

        private String status; // e.g. Finished Airing

        private Images images;

        private List<Genre> genres;

        private List<Studio> studios;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Images {
        private Jpg jpg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Jpg {
        @JsonProperty("large_image_url")
        private String largeImageUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Genre {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Studio {
        private String name;
    }
}
