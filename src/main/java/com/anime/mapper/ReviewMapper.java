package com.anime.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.anime.domain.ReviewDetailDto;
import com.anime.domain.ReviewDto;

@Mapper
public interface ReviewMapper {

    List<ReviewDto> findByAnime(@Param("malAnimeId") int malAnimeId);

    List<ReviewDto> findByUser(@Param("userId") long userId);

    ReviewDto findById(@Param("id") long id);

    ReviewDetailDto findDetail(@Param("id") long id);

    int insert(@Param("userId") long userId,
               @Param("malAnimeId") int malAnimeId,
               @Param("title") String title,
               @Param("content") String content);

    int updateByOwner(@Param("id") long id,
                      @Param("userId") long userId,
                      @Param("title") String title,
                      @Param("content") String content);

    int deleteByOwner(@Param("id") long id,
                      @Param("userId") long userId);

    int deleteById(@Param("id") long id); // 관리자용
    
    //메인에 출력할 리뷰리스트
    List<ReviewDto> findRecent(@Param("limit") int limit);
    
    /** 관리자: 전체 리뷰 목록 (신고순 정렬 옵션) */
    List<ReviewDto> findAll();

    /** 신고: report_count + 1 */
    int incrementReportCount(@Param("id") long id);

    /** 관리자: 신고수 초기화 */
    int resetReportCount(@Param("id") long id);
}

