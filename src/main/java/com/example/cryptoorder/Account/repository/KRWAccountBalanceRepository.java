package com.example.cryptoorder.Account.repository;

import com.example.cryptoorder.Account.entity.KRWAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KRWAccountBalanceRepository extends JpaRepository<KRWAccount, UUID> {
}