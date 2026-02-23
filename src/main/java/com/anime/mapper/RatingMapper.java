package com.anime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RatingMapper {
    int upsert(@Param("userId") long userId,
               @Param("malId") int malId,
               @Param("score") int score);

    int deleteByUserAndAnime(@Param("userId") long userId,
                             @Param("malId") int malId);

    Integer findMyScore(@Param("userId") long userId,
                        @Param("malId") int malId); // (수정폼에 점수 미리 채우려면)
}
