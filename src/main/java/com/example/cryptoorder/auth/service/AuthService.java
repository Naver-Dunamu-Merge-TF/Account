package com.example.cryptoorder.auth.service;

import com.example.cryptoorder.Account.entity.Account;
import com.example.cryptoorder.Account.entity.User;
import com.example.cryptoorder.Account.repository.AccountRepository;
import com.example.cryptoorder.Account.service.UserAccountService;
import com.example.cryptoorder.auth.config.AuthServerProperties;
import com.example.cryptoorder.auth.dto.AuthLoginRequest;
import com.example.cryptoorder.auth.dto.AuthRefreshRequest;
import com.example.cryptoorder.auth.dto.AuthSignupRequest;
import com.example.cryptoorder.auth.security.JwtTokenService;
import com.example.cryptoorder.common.api.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountService userAccountService;
    private final AccountRepository accountRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final CustodyProvisionClient custodyProvisionClient;
    private final AuthServerProperties authServerProperties;

    @Transactional(rollbackFor = Exception.class)
    public TokenPair signup(AuthSignupRequest request) {
        String provider = resolveProvider(request.provider());

        User user = userAccountService.createFullAccount(
                request.name(),
                request.phone(),
                request.birthDate(),
                request.loginId(),
                request.password(),
                provider,
                request.externalUserId()
        );

        custodyProvisionClient.provisionWallet(user.getId(), provider, request.externalUserId());

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("계정 정보를 찾을 수 없습니다."));

        return issueTokenPair(user, account);
    }

    @Transactional
    public TokenPair login(AuthLoginRequest request) {
        try {
            User user = userAccountService.login(request.loginId(), request.password());
            Account account = accountRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalStateException("계정 정보를 찾을 수 없습니다."));
            return issueTokenPair(user, account);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public TokenPair refresh(AuthRefreshRequest request) {
        User user = refreshTokenService.rotate(request.refreshToken());
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("계정 정보를 찾을 수 없습니다."));
        return issueTokenPair(user, account);
    }

    private TokenPair issueTokenPair(User user, Account account) {
        String accessToken = jwtTokenService.issueAccessToken(
                user.getId(),
                account.getAuthProvider(),
                account.getExternalUserId()
        );
        IssuedRefreshToken issuedRefreshToken = refreshTokenService.issue(user);

        return new TokenPair(
                accessToken,
                issuedRefreshToken.plainToken(),
                authServerProperties.getAccessTokenTtlSeconds(),
                issuedRefreshToken.expiresIn(),
                user.getId()
        );
    }

    private String resolveProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return "MEMBER";
        }
        return provider;
    }
}
