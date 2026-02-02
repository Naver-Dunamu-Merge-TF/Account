package com.example.cryptoorder.Account.repository;

import com.example.cryptoorder.Account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}