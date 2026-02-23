package com.anime.domain;

import lombok.Data;

/**
 * 회원가입 폼 수신 전용 DTO
 * - 평문 비밀번호는 이 객체에서만 다루고 User 도메인에는 넣지 않는다.
 */
@Data
public class RegisterForm {
    private String username;
    private String password;       // 평문 비밀번호 (폼에서만 사용, User 도메인으로 옮기지 않음)
    private String passwordConfirm; // 확인용 (선택)
    private String nickname;
    private String email;
}