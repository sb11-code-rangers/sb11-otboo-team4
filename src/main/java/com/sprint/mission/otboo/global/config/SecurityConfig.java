package com.sprint.mission.otboo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.csrf(AbstractHttpConfigurer::disable);
        // TODO: 개발 단계에서 csrf 비활성화 / 추후 csrf 설정 추가

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // TODO: Security Exception 설정 추가

        // TODO: JwtAuthenticationFilter 설정 추가

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
                // TODO: 임시로 모든 경로 허용 (JWT 인증 프로세스 도입 후 경로별 인가 설정 추가)
        );

        // TODO: OAuth2 설정 추가

        return http.build();
    }
}
