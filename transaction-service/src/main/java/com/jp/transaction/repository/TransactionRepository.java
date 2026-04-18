package com.jp.transaction.repository;

import com.jp.transaction.entity.Transaction;
import com.jp.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderId(Long senderId);
    List<Transaction> findByReceiverId(Long receiverId);
    List<Transaction> findByStatus(TransactionStatus status);
    Optional<Transaction> findByTransactionRef(String transactionRef);
}
