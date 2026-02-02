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
    //DB툴에서 조회시 SELECT HEX(user_uuid), user_name FROM users;와 같이 HEX함수를 사용하여 조회 필요
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
