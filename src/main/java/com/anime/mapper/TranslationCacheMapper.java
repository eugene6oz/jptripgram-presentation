package com.anime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TranslationCacheMapper {

    String findTitle(@Param("malId") int malId,
                     @Param("lang") String lang);

    String findSynopsis(@Param("malId") int malId,
                        @Param("lang") String lang);

    void upsert(@Param("malId") int malId,
                @Param("lang") String lang,
                @Param("title") String title,
                @Param("synopsis") String synopsis);
}
