package com.example.cryptoorder.Account.repository;

import com.example.cryptoorder.Account.entity.KRWAccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KRWAccountBalanceRepository extends JpaRepository<KRWAccountBalance, UUID> {
}