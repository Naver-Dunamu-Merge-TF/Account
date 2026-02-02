package com.example.cryptoorder.Account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name="krw_account_balances")
public class KRWAccountBalance {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    //공간 절약과 인덱스 성능 향상을 위해서 BINARY(16)으로 컬럼 타입 설정
    @Column(name="user_uuid",columnDefinition = "BINARY(16)")
    //내부 계좌 관리 id (외부에 노출되지 않음)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User ownerUserId;

    //계좌 번호 (실제 입출금에 사용되는 번호)
    //계좌 번호 규정에 따른 검증 로직 추가 필요
    private String accountNumber;

    private String ownerName;

    private boolean isActive;

    private Long balance;

    @UpdateTimestamp
    private LocalDateTime updateTime;

}
