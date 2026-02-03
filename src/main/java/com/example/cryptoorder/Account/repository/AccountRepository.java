package com.example.cryptoorder.Account.repository;

import com.example.cryptoorder.Account.entity.Account;
import com.example.cryptoorder.Account.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByUser(User user);


}