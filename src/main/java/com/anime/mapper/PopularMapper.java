package com.anime.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PopularMapper {

	    List<Integer> topAnimeIdsByFavorites(@Param("limit") int limit);
}
	
