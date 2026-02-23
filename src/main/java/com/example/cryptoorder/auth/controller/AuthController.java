package com.example.cryptoorder.auth.controller;

import com.example.cryptoorder.auth.dto.AuthLoginRequest;
import com.example.cryptoorder.auth.dto.AuthRefreshRequest;
import com.example.cryptoorder.auth.dto.AuthSignupRequest;
import com.example.cryptoorder.auth.dto.AuthTokenResponse;
import com.example.cryptoorder.auth.service.AuthService;
import com.example.cryptoorder.auth.service.TokenPair;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthTokenResponse> signup(@RequestBody @Valid AuthSignupRequest request) {
        TokenPair pair = authService.signup(request);
        return ResponseEntity.ok(toResponse(pair));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@RequestBody @Valid AuthLoginRequest request) {
        TokenPair pair = authService.login(request);
        return ResponseEntity.ok(toResponse(pair));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@RequestBody @Valid AuthRefreshRequest request) {
        TokenPair pair = authService.refresh(request);
        return ResponseEntity.ok(toResponse(pair));
    }

    private AuthTokenResponse toResponse(TokenPair pair) {
        return new AuthTokenResponse(
                "Bearer",
                pair.accessToken(),
                pair.accessTokenExpiresIn(),
                pair.refreshToken(),
                pair.refreshTokenExpiresIn(),
                pair.userId()
        );
    }
}
