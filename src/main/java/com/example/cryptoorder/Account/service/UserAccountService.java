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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final KRWAccountBalanceRepository krwRepository;
    private final NaverPointRepository naverPointRepository;

    @Transactional
    public void registerUser(String name, String phone, LocalDate age) {
        // 빌더 패턴 사용: 파라미터 순서 상관없음, 필드명 명시로 가독성 향상
        User newUser = User.builder()
                .userName(name)
                .phoneNumber(phone)
                .userAge(age)
                .build();

        userRepository.save(newUser);

    }
}