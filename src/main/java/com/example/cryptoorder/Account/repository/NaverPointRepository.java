package com.example.cryptoorder.Account.repository;

import com.example.cryptoorder.Account.entity.Account;
import com.example.cryptoorder.Account.entity.NaverPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NaverPointRepository extends JpaRepository<NaverPoint, Account> {
}