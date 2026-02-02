package com.example.cryptoorder.Account.service;

import com.example.cryptoorder.Account.entity.Account;
import com.example.cryptoorder.Account.entity.KRWAccountBalance;
import com.example.cryptoorder.Account.entity.NaverPoint;
import com.example.cryptoorder.Account.entity.User;
import com.example.cryptoorder.Account.repository.AccountRepository;
import com.example.cryptoorder.Account.repository.KRWAccountBalanceRepository;
import com.example.cryptoorder.Account.repository.NaverPointRepository;
import com.example.cryptoorder.Account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final KRWAccountBalanceRepository krwRepository;
    private final NaverPointRepository naverPointRepository;

    @Transactional
    public void createFullAccount(String name, String phone, LocalDate age, String loginId, String password) {
        // 1. User 생성
        User newUser = User.builder()
                        .userName(name)
                        .phoneNumber(phone)
                        .userAge(age)
                        .build();
        userRepository.save(newUser);

        // 2. Account (로그인 정보) 생성 및 User 연결
        Account newAccount = Account.builder()
                .user(newUser)
                .userLoginId(loginId)
                .userLoginPw(passwordEncoder.encode(password)) // 추후 암호화 필요
                .build();
        accountRepository.save(newAccount);

        // 3. 포인트 지갑 생성
        NaverPoint newPoint = NaverPoint.builder()
                .account(newAccount)
                .balance(0L)
                .build();
        naverPointRepository.save(newPoint);
    }
}