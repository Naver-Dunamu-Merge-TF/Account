package com.example.cryptoorder.Account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class NaverPoint {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    // 식별관계인 계정의 PK를 외래키로 참조
    private Account account;

    private Long balance;

    @UpdateTimestamp
    private LocalDateTime updateTime;


}
