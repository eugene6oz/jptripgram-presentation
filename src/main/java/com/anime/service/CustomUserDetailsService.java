package com.anime.service;

import com.anime.config.CustomUserDetails;
import com.anime.domain.User;
import com.anime.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * CustomUserDetailsService
 * - Spring Security가 사용자 정보를 조회할 때 사용
 * - UserDetailsService 인터페이스 구현 필수
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 사용자명으로 사용자 정보 조회
     * @param username 로그인 시 입력한 사용자명
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 사용자 조회
        User user = userMapper.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("User not found");

//        // 2. 사용자가 없으면 예외 발생
//        if (user == null) {
//            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
//        }
//        
//        System.out.println("pw=[" + user.getPassword() + "], len=" + user.getPassword().length());
//        System.out.println("role=" + user.getRole() + ", enabled=" + user.isEnabled());
//
//        // 3. Spring Security의 User 객체로 변환하여 반환
//        return org.springframework.security.core.userdetails.User.builder()
//                .username(user.getUsername())
//                .password(user.getPassword())  // 이미 암호화된 비밀번호
//                .authorities(Collections.singletonList(
//                        new SimpleGrantedAuthority(user.getRole())
//                ))
//                .disabled(!user.isEnabled())
//                .build();
//        
        
        return new CustomUserDetails(user);
    }
}