package com.anime.service;

import com.anime.config.CustomUserDetails;
import com.anime.domain.ProfileForm;
import com.anime.domain.RegisterForm;
import com.anime.domain.User;
import com.anime.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────
    // 인증
    // ─────────────────────────────────────────

    public boolean registerUser(RegisterForm form) {
        if (userMapper.existsByUsername(form.getUsername()) > 0) return false;
        if (form.getPassword() == null || form.getPassword().isBlank())
            throw new IllegalArgumentException("비밀번호가 비었습니다.");

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setNickname(form.getNickname());
        user.setEmail(form.getEmail());
        user.setRole("ROLE_USER");
        user.setStatus("ACTIVE");
        userMapper.insertUser(user);
        return true;
    }

    public User findByUsername(String username) { return userMapper.findByUsername(username); }
    public boolean isUsernameTaken(String username) { return userMapper.existsByUsername(username) > 0; }
    public void updateLastLogin(String username) { userMapper.updateLastLogin(username); }

    /** Security principal 에서 userId 직접 추출 (DB 조회 없음) */
    public long getUserId(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    // ─────────────────────────────────────────
    // 회원정보 수정
    // ─────────────────────────────────────────

    /**
     * 닉네임/이메일 수정 + 비밀번호 변경(입력 시)
     * @return null = 성공, 문자열 = 에러메시지
     */
    @Transactional
    public String updateProfile(long userId, ProfileForm form) {

        // 1) 닉네임/이메일 수정
        User user = new User();
        user.setId(userId);
        user.setNickname(form.getNickname());
        user.setEmail(form.getEmail());
        userMapper.updateProfile(user);

        // 2) 비밀번호 변경 (입력했을 때만)
        String newPw = form.getNewPassword();
        if (newPw != null && !newPw.isBlank()) {
            User current = userMapper.findById(userId);
            if (!passwordEncoder.matches(form.getCurrentPassword(), current.getPasswordHash()))
                return "현재 비밀번호가 올바르지 않습니다.";
            if (!newPw.equals(form.getNewPasswordConfirm()))
                return "새 비밀번호가 일치하지 않습니다.";
            if (newPw.length() < 4)
                return "비밀번호는 4자 이상이어야 합니다.";

            user.setPasswordHash(passwordEncoder.encode(newPw));
            userMapper.updatePassword(user);
        }
        return null; // 성공
    }

    // ─────────────────────────────────────────
    // 회원 탈퇴
    // ─────────────────────────────────────────

    /**
     * 비밀번호 확인 후 완전 삭제
     * @return true = 성공, false = 비밀번호 불일치
     */
    @Transactional
    public boolean withdraw(long userId, String inputPassword) {
        User user = userMapper.findById(userId);
        if (user == null) return false;
        if (!passwordEncoder.matches(inputPassword, user.getPasswordHash())) return false;
        userMapper.deleteUser(userId);
        return true;
    }

    // ─────────────────────────────────────────
    // 관리자
    // ─────────────────────────────────────────

    public List<User> findAllUsers() { return userMapper.findAll(); }

    @Transactional
    public void deleteUserByAdmin(Long targetId) { userMapper.deleteUserById(targetId); }
}
