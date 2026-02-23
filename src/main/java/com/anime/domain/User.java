package com.anime.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User {

    private Long id;

    private String username;
    private String passwordHash;   // DB: password_hash (평문 password 필드 제거)

    private String nickname;
    private String email;

    private String role;           // DB: role (ROLE_USER / ROLE_ADMIN)
    private String status;         // DB: status (ACTIVE / SUSPENDED / DELETED)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}