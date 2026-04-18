package com.jp.transaction.service;

import com.jp.transaction.dto.TransactionDTO;
import com.jp.transaction.entity.Transaction;
import com.jp.transaction.entity.TransactionStatus;
import com.jp.transaction.entity.TransactionType;
import com.jp.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionDTO createTransaction(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setTransactionRef(generateTransactionRef());
        transaction.setSenderId(dto.getSenderId());
        transaction.setReceiverId(dto.getReceiverId());
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "USD");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionType(TransactionType.valueOf(dto.getTransactionType()));
        transaction.setDescription(dto.getDescription());
        return mapToDTO(transactionRepository.save(transaction));
    }

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return mapToDTO(transaction);
    }

    public List<TransactionDTO> getTransactionsBySenderId(Long senderId) {
        return transactionRepository.findBySenderId(senderId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByReceiverId(Long receiverId) {
        return transactionRepository.findByReceiverId(receiverId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO updateTransactionStatus(Long id, String status) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        transaction.setStatus(TransactionStatus.valueOf(status));
        return mapToDTO(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    private String generateTransactionRef() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionRef(transaction.getTransactionRef());
        dto.setSenderId(transaction.getSenderId());
        dto.setReceiverId(transaction.getReceiverId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setStatus(transaction.getStatus().name());
        dto.setTransactionType(transaction.getTransactionType().name());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }
}
