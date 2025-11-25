package ru.kpfu.itis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.dto.ChargeDto;
import ru.kpfu.itis.dto.CreateAccountDto;
import ru.kpfu.itis.model.Account;
import ru.kpfu.itis.model.PaymentTransaction;
import ru.kpfu.itis.repository.AccountRepository;
import ru.kpfu.itis.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final AccountRepository accountRepository;
    private final PaymentTransactionRepository transactionRepository;


    @GetMapping("/users")
    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    @GetMapping("/transactions")
    public List<PaymentTransaction> getTransactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/users/{username}")
    public Account getUser(@PathVariable String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow();
    }

    @PostMapping("/create-account")
    public ResponseEntity<String> createAccount(@RequestBody CreateAccountDto dto) {
        Account acc = Account.builder()
                .username(dto.username())
                .balance(dto.balance())
                .build();
        accountRepository.save(acc);
        return ResponseEntity.ok("ACCOUNT_CREATED");
    }

}


