package com.example.myFarm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [신규] AuthenticationManager를 Bean으로 등록합니다.
     * (MemberController에서 수동 로그인을 위해 필요)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (JS에서 API 호출)

                .authorizeHttpRequests(authz -> authz
                        // 1. 인증 없이 접근 허용
                        .requestMatchers(
                                "/", "/index.html",
                                "/css/**", "/js/**",        // 정적 리소스
                                "/api/member/signup",      // 회원가입 API
                                "/api/member/login"        // 로그인 API
                        ).permitAll()

                        // 2. 인증이 필요한 URL
                        .requestMatchers(
                                "/api/cart/**",            // 장바구니 API
                                "/api/member/me",          // 내 정보 API
                                "/checkout", "/checkout.html" // 결제 페이지
                        ).authenticated() // '인증된 사용자'만 접근 가능

                        .anyRequest().permitAll()
                )

                // [수정] 폼 로그인/로그아웃 비활성화 (AJAX로 직접 처리)
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())

                // [신규] 인증되지 않은 사용자가 /api/cart/** 등
                //       '인증이 필요한 URL'에 접근 시,
                //       페이지 리다이렉트 대신 401 Unauthorized 에러를 반환
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        )
                );

        return http.build();
    }
}