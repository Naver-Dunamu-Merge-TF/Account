package com.example.cryptoorder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 1. 기본으로 사용할 암호화 ID 지정 (현재 가장 무난한 bcrypt 사용)
        String idForEncode = "bcrypt";

        // 2. 지원할 암호화 인코더 목록 생성
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        // Bcrypt 설정 (Strength 10~12 권장)
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        // 추후 컴플라이언스 기준에 맞춰서 암호화 강화 필요 시 전환 가능

        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }
}
