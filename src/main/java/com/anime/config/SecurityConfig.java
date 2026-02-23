package com.anime.config;

import com.anime.service.CustomUserDetailsService;
import com.anime.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home", "/login", "/register",
                    "/css/**", "/js/**", "/images/**",
                    // ✅ 비로그인도 검색·상세·리뷰 조회 허용
                    "/search", "/anime/**", "/reviews/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // ✅ 번역 API: 비로그인은 search 페이지 접근 불가하므로 사실상 인증 필요
                //    단, search도 permitAll로 바꿨으니 anonymous도 호출 가능 → authenticated 유지
                .requestMatchers("/api/translate").authenticated()
                .requestMatchers("/api/favorites/**").authenticated()
                .requestMatchers("/anime/api/file/**").hasRole("ADMIN")
                .requestMatchers("/anime/api/list").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(loginSuccessHandler())
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException {
                userService.updateLastLogin(authentication.getName());
                response.sendRedirect("/home");
            }
        };
    }
}