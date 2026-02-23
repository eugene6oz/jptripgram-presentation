package com.anime.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anime.domain.ReviewDetailDto;
import com.anime.domain.ReviewDto;
import com.anime.mapper.RatingMapper;
import com.anime.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final RatingMapper ratingMapper;

    public List<ReviewDto> listByAnime(int malAnimeId) { return reviewMapper.findByAnime(malAnimeId); }
    public List<ReviewDto> listByUser(long userId)     { return reviewMapper.findByUser(userId); }
    public ReviewDto get(long id)                      { return reviewMapper.findById(id); }
    public ReviewDetailDto getDetail(long id)          { return reviewMapper.findDetail(id); }
    public List<ReviewDto> listRecent(int limit)       { return reviewMapper.findRecent(limit); }

    /** 관리자: 전체 리뷰 목록 (신고 많은 순) */
    public List<ReviewDto> listAll()                   { return reviewMapper.findAll(); }

    @Transactional
    public void create(long userId, int malId, String title, String content, Integer score) {
        reviewMapper.insert(userId, malId, title, content);
        applyRating(userId, malId, score);
    }

    @Transactional
    public boolean updateByOwner(long reviewId, long userId, String title, String content, Integer score, int malId) {
        int updated = reviewMapper.updateByOwner(reviewId, userId, title, content);
        if (updated == 1) { applyRating(userId, malId, score); return true; }
        return false;
    }

    @Transactional
    public boolean deleteByOwner(long id, long userId) { return reviewMapper.deleteByOwner(id, userId) == 1; }

    @Transactional
    public void deleteByAdmin(long id) { reviewMapper.deleteById(id); }

    /** 신고: report_count + 1 */
    @Transactional
    public void report(long reviewId) { reviewMapper.incrementReportCount(reviewId); }

    /** 관리자: 신고수 초기화 */
    @Transactional
    public void resetReportCount(long reviewId) { reviewMapper.resetReportCount(reviewId); }

    private void applyRating(long userId, int malId, Integer score) {
        if (score == null) { ratingMapper.deleteByUserAndAnime(userId, malId); return; }
        ratingMapper.upsert(userId, malId, Math.max(1, Math.min(10, score)));
    }
}
