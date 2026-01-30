package com.example.cryptoorder.Account.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
public class KRWTransaction {
    @Id
    private String transactionId;

    private String Sender;

    private String Receiver;

    private Long amount;

    private String Status;

    @CreationTimestamp
    private LocalDateTime transactionDate;


}
