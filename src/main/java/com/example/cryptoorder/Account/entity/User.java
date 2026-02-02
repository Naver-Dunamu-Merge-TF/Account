package com.example.cryptoorder.Account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name="users")
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    //공간 절약과 인덱스 성능 향상을 위해서 BINARY(16)으로 컬럼 타입 설정
    @Column(name="user_uuid",columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userAge;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
    private String phoneNumber;





}
