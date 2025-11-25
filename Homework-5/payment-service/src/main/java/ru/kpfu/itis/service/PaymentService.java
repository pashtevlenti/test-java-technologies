package ru.kpfu.itis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.model.Account;
import ru.kpfu.itis.model.Outbox;
import ru.kpfu.itis.model.PaymentTransaction;
import ru.kpfu.itis.repository.AccountRepository;
import ru.kpfu.itis.repository.OutboxRepository;
import ru.kpfu.itis.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void handlePaymentRequest(UUID sagaId, Integer orderId, String username, BigDecimal amount) {

        if (transactionRepository.findBySagaId(sagaId).isPresent()) return;

        accountRepository.findByUsername(username).ifPresentOrElse(account -> {
            boolean success = account.getBalance().compareTo(amount) >= 0;

            Account updatedAccount;
            if (success) {
                updatedAccount = Account.builder()
                        .id(account.getId())
                        .username(account.getUsername())
                        .balance(account.getBalance().subtract(amount))
                        .build();
                accountRepository.save(updatedAccount);
            }

            PaymentTransaction tx = PaymentTransaction.builder()
                    .sagaId(sagaId)
                    .accountId(account.getId())
                    .amount(amount)
                    .success(success)
                    .build();
            transactionRepository.save(tx);

            String topic = success ? "payment.paid" : "payment.failed";

            Outbox out = Outbox.builder()
                    .sagaId(sagaId)
                    .topic(topic)
                    .payload("{\"sagaId\":%d,\"orderId\":%d}".formatted(sagaId, orderId))
                    .build();

            outboxRepository.save(out);

        }, () -> {
            Outbox out = Outbox.builder()
                    .sagaId(sagaId)
                    .topic("payment.failed")
                    .payload("{\"sagaId\":%d,\"orderId\":%d}".formatted(sagaId, orderId))
                    .build();
            outboxRepository.save(out);
        });
    }

    @Transactional
    public void refund(UUID sagaId) {
        transactionRepository.findBySagaId(sagaId).ifPresent(tx -> {
            if (!tx.isSuccess()) return;

            accountRepository.findById(tx.getAccountId()).ifPresent(account -> {

                Account updated = Account.builder()
                        .id(account.getId())
                        .username(account.getUsername())
                        .balance(account.getBalance().add(tx.getAmount()))
                        .build();
                accountRepository.save(updated);
            });

            tx = PaymentTransaction.builder()
                    .id(tx.getId())
                    .sagaId(tx.getSagaId())
                    .accountId(tx.getAccountId())
                    .amount(tx.getAmount())
                    .success(false)
                    .build();
            transactionRepository.save(tx);
        });
    }
}
