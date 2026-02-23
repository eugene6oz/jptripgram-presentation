package com.anime.domain;

import lombok.Data;

/** 회원정보 수정 폼 (닉네임, 이메일, 비밀번호) */
@Data
public class ProfileForm {
    private String nickname;
    private String email;

    // 비밀번호 변경 (입력 안 하면 null → 변경 안 함)
    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;
}