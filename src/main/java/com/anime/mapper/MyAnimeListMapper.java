package com.anime.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyAnimeListMapper {

    // 즐겨찾기 여부 (0/1)
    int isFavorite(@Param("userId") long userId,
                   @Param("malId") int malId);

    // 즐겨찾기 목록 malId들
    List<Integer> findFavoriteIds(@Param("userId") long userId,
                                  @Param("limit") int limit);

    // 즐겨찾기 ON (없으면 insert, 있으면 update)
    int favoriteOn(@Param("userId") long userId,
                   @Param("malId") int malId);

    // 즐겨찾기 OFF (row는 유지하고 is_favorite=0)
    int favoriteOff(@Param("userId") long userId,
                    @Param("malId") int malId);
}

