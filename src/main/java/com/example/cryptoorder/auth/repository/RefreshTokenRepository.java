package com.example.cryptoorder.auth.repository;

import com.example.cryptoorder.Account.entity.User;
import com.example.cryptoorder.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    void deleteAllByUser(User user);
}
