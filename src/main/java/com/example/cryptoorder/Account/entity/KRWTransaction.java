package com.example.cryptoorder.Account.entity;

import com.example.cryptoorder.Account.constant.TransactionStatus;
import com.example.cryptoorder.Account.constant.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="krw_transactions")
public class KRWTransaction {
    @Id
    private String transactionId;

    private String sender;

    private String receiver;

    private Long amount;

    private TransactionStatus status;

    private TransactionType transactionType;

    @CreationTimestamp
    private LocalDateTime transactionDate;


}
