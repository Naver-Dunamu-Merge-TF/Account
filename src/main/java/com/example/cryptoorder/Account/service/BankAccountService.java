package com.example.cryptoorder.Account.service;

import com.example.cryptoorder.Account.constant.TransactionStatus;
import com.example.cryptoorder.Account.constant.TransactionType;
import com.example.cryptoorder.Account.entity.KRWAccount;
import com.example.cryptoorder.Account.entity.KRWAccountHistory;
import com.example.cryptoorder.Account.entity.KRWTransaction;
import com.example.cryptoorder.Account.repository.KRWAccountBalanceRepository;
import com.example.cryptoorder.Account.repository.KRWAccountHistoryRepository;
import com.example.cryptoorder.Account.repository.KRWTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {


    private final KRWAccountBalanceRepository accountRepository;
    private final KRWTransactionRepository transactionRepository;
    private final KRWAccountHistoryRepository accountHistoryRepository;


    /**
     *  입금
     */
    @Transactional
    public KRWAccountHistory deposit(UUID KRWaccountId, Long amount, String sender) {
        //1. 비관적 락을 사용하여 계좌 조회
        KRWAccount account = accountRepository.findByIdWithLock(KRWaccountId)
                .orElseThrow(()->new IllegalArgumentException("계좌를 찾을 수 없습니다!"));

        //2. 잔고 증가
        account.deposit(amount);

        //3. 거래 기록 생성(KRWTeansaction)
        // 추후 DTO 생성시 DTO로 로직 이전
        // 추후 실패 에러 발생시 예외 설정하여 상태에 반영해서 저장하는 로직 추가 필요
        KRWTransaction transaction = KRWTransaction.builder()
                .sender(sender)
                .receiver(account.getAccountNumber())
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.DEPOSIT)
                .relatedUserId(account.getUser())
                .build();
        transactionRepository.save(transaction);

        //4. 계좌 기록 저장
        KRWAccountHistory transactionAccountHistory = createAccountHistory(account, transaction, TransactionType.DEPOSIT, amount);
        return accountHistoryRepository.save(transactionAccountHistory);

    }



    // 계좌 기록 생성 메서드
    private KRWAccountHistory createAccountHistory(KRWAccount account, KRWTransaction transaction,
                                                  TransactionType type, Long amount) {
        Long balanceAfter = account.getBalance();
        KRWAccountHistory history = KRWAccountHistory.builder()
                .bankAccount(account)
                .transaction(transaction)
                .transactionType(type)
                .amount(amount)
                .balanceAfterTransaction(balanceAfter)
                .build();
        return history;
    }

}

