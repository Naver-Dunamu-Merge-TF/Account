package com.example.cryptoorder.auth.security;

import com.example.cryptoorder.auth.config.AuthServerProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtKeyProvider {

    private final AuthServerProperties authServerProperties;

    private RSAPrivateKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;
    private byte[] hmacSecret;

    @PostConstruct
    void init() {
        if (authServerProperties.getMode() == AuthServerProperties.Mode.JWKS) {
            initRsaKeys();
        } else {
            initHmacSecret();
        }
    }

    public RSAPrivateKey getRsaPrivateKey() {
        if (rsaPrivateKey == null) {
            throw new IllegalStateException("JWKS 모드에서만 RSA 개인키를 사용할 수 있습니다.");
        }
        return rsaPrivateKey;
    }

    public byte[] getHmacSecret() {
        if (hmacSecret == null) {
            throw new IllegalStateException("HMAC 모드에서만 HMAC 시크릿을 사용할 수 있습니다.");
        }
        return hmacSecret;
    }

    public RSAKey getPublicRsaJwk() {
        if (rsaPublicKey == null) {
            throw new IllegalStateException("JWKS 모드에서만 JWKS 공개키를 사용할 수 있습니다.");
        }

        return new RSAKey.Builder(rsaPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(authServerProperties.getJwtKeyId())
                .build();
    }

    private void initRsaKeys() {
        try {
            String privateKeyBase64 = authServerProperties.getJwtPrivateKeyBase64();
            String publicKeyBase64 = authServerProperties.getJwtPublicKeyBase64();

            if (isNotBlank(privateKeyBase64) && isNotBlank(publicKeyBase64)) {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                byte[] privateBytes = Base64.getDecoder().decode(stripWhitespace(privateKeyBase64));
                byte[] publicBytes = Base64.getDecoder().decode(stripWhitespace(publicKeyBase64));

                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
                PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
                this.rsaPrivateKey = (RSAPrivateKey) privateKey;
                this.rsaPublicKey = (RSAPublicKey) publicKey;
                return;
            }

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        } catch (Exception e) {
            throw new IllegalStateException("JWT RSA 키 초기화에 실패했습니다.", e);
        }
    }

    private void initHmacSecret() {
        String secretBase64 = authServerProperties.getHmacSecretBase64();
        if (!isNotBlank(secretBase64)) {
            throw new IllegalStateException("HMAC 모드에서는 auth.hmac-secret-base64가 필수입니다.");
        }

        byte[] decoded = Base64.getDecoder().decode(stripWhitespace(secretBase64));
        if (decoded.length < 32) {
            throw new IllegalStateException("HMAC 시크릿은 최소 256-bit(32바이트) 이상이어야 합니다.");
        }
        this.hmacSecret = decoded;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String stripWhitespace(String value) {
        return value.replaceAll("\\s", "");
    }
}
