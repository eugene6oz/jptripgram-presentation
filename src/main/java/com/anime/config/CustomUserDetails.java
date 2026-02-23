package com.anime.config;

import com.anime.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String nickname;
    private final String username;
    private final String password;  // UserDetails가 요구하는 필드명이라 그대로 둠
    private final String email;
    private final String status;    // ACTIVE / SUSPENDED / DELETED
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.email = user.getEmail();
        this.status = user.getStatus(); // ✅ enabled 대신 status
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole())); // ROLE_USER/ROLE_ADMIN
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // ✅ status가 ACTIVE일 때만 로그인 허용
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }
}
