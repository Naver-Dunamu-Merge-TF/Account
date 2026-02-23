package com.example.cryptoorder.auth.service;

import com.example.cryptoorder.auth.config.CustodyProvisionProperties;
import com.example.cryptoorder.auth.dto.CustodyWalletProvisionRequest;
import com.example.cryptoorder.auth.dto.CustodyWalletProvisionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustodyProvisionClient {

    private final CustodyProvisionProperties custodyProvisionProperties;
    private final RestClient.Builder restClientBuilder;

    public void provisionWallet(UUID userId, String provider, String externalUserId) {
        if (!custodyProvisionProperties.isEnabled()) {
            return;
        }

        validateConfiguration();

        RestClient restClient = restClientBuilder
                .baseUrl(custodyProvisionProperties.getBaseUrl())
                .build();

        CustodyWalletProvisionRequest request = new CustodyWalletProvisionRequest(userId, provider, externalUserId);
        String idempotencyKey = "member-signup-" + userId;

        try {
            ResponseEntity<CustodyWalletProvisionResponse> response = restClient.post()
                    .uri(custodyProvisionProperties.getPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(custodyProvisionProperties.getServiceTokenHeader(), custodyProvisionProperties.getServiceToken())
                    .header("Idempotency-Key", idempotencyKey)
                    .body(request)
                    .retrieve()
                    .toEntity(CustodyWalletProvisionResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("커스터디 지갑 프로비저닝에 실패했습니다. status=" + response.getStatusCode().value());
            }
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("커스터디 지갑 프로비저닝에 실패했습니다. status=" + e.getStatusCode().value(), e);
        }
    }

    private void validateConfiguration() {
        if (isBlank(custodyProvisionProperties.getBaseUrl())) {
            throw new IllegalStateException("custody.provision.base-url이 필요합니다.");
        }
        if (isBlank(custodyProvisionProperties.getServiceToken())) {
            throw new IllegalStateException("custody.provision.service-token이 필요합니다.");
        }
        if (isBlank(custodyProvisionProperties.getServiceTokenHeader())) {
            throw new IllegalStateException("custody.provision.service-token-header가 필요합니다.");
        }
        if (isBlank(custodyProvisionProperties.getPath())) {
            throw new IllegalStateException("custody.provision.path가 필요합니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
