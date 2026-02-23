package com.example.cryptoorder.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "auth.mode=JWKS",
        "custody.provision.enabled=false"
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_issuesAccessAndRefreshTokenWithRequiredClaims() throws Exception {
        String response = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"회원A",
                                  "phone":"010-1234-5678",
                                  "birthDate":"1990-01-01",
                                  "loginId":"member-a",
                                  "password":"password123",
                                  "provider":"MEMBER",
                                  "externalUserId":"ext-member-a"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        String accessToken = body.get("accessToken").asText();
        SignedJWT jwt = SignedJWT.parse(accessToken);

        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo("rwa-id-server");
        assertThat(jwt.getJWTClaimsSet().getAudience()).contains("rwa-custody");
        assertThat(jwt.getJWTClaimsSet().getSubject()).isNotBlank();
        assertThat(jwt.getJWTClaimsSet().getClaim("provider")).isEqualTo("MEMBER");
        assertThat(jwt.getJWTClaimsSet().getClaim("externalUserId")).isEqualTo("ext-member-a");
        assertThat(jwt.getJWTClaimsSet().getClaim("roles")).isEqualTo(List.of("USER"));
        assertThat(jwt.getJWTClaimsSet().getExpirationTime()).isAfter(jwt.getJWTClaimsSet().getIssueTime());
    }

    @Test
    void login_and_refresh_rotateRefreshToken() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"회원B",
                                  "phone":"010-9999-0000",
                                  "birthDate":"1992-02-02",
                                  "loginId":"member-b",
                                  "password":"password123"
                                }
                                """))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId":"member-b",
                                  "password":"password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstRefresh = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        String refreshResponse = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken":"%s"
                                }
                                """.formatted(firstRefresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondRefresh = objectMapper.readTree(refreshResponse).get("refreshToken").asText();
        assertThat(secondRefresh).isNotEqualTo(firstRefresh);
    }

    @Test
    void jwks_endpoint_exposesPublicKey() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kid").value("member-server-key-1"));
    }

    @Test
    void login_withInvalidCredential_returns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId":"unknown",
                                  "password":"wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
