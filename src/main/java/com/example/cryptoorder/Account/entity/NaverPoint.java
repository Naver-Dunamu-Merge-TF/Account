package com.example.cryptoorder.Account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name="naver_points")
public class NaverPoint {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @NotNull
    // 식별관계인 계정의 PK를 외래키로 참조
    private Account account;

    @NotNull
    private Long balance;

    @UpdateTimestamp
    private LocalDateTime updateTime;


}
