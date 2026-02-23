package com.anime.mapper;

import com.anime.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User findByUsername(String username);
    User findById(Long id);               // ✅ 추가: id로 직접 조회
    Long findIdByUsername(String username);
    void insertUser(User user);
    int existsByUsername(String username);
    void updateLastLogin(String username);

    /** 닉네임/이메일 수정 */
    void updateProfile(User user);

    /** 비밀번호 변경 */
    void updatePassword(User user);

    /** 회원 탈퇴 (완전 삭제) */
    void deleteUser(Long id);

    /** 관리자: 전체 회원 목록 */
    java.util.List<User> findAll();

    /** 관리자: 회원 강제 탈퇴 */
    void deleteUserById(Long id);
}