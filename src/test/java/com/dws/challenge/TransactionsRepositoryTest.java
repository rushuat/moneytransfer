package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transaction;
import com.dws.challenge.exception.InvalidTransactionAmountException;
import com.dws.challenge.exception.MissingAccountIdException;
import com.dws.challenge.exception.NegativeAccountBalanceException;
import com.dws.challenge.exception.SingleAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.TransactionsRepository;
import com.dws.challenge.util.Accounts;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionsRepositoryTest {

  @Autowired
  private AccountsRepository accountsRepository;
  @Autowired
  private TransactionsRepository transactionsRepository;

  @BeforeEach
  void refreshRepository() {
    // Reset the existing accounts before each test.
    accountsRepository.clearAccounts();
  }

  @Test
  void applyTransaction_failsOnMissingFromAccountId() {
    String fromId = "Id-" + System.currentTimeMillis();
    Transaction transaction = new Transaction(fromId, "Id-123", BigDecimal.valueOf(1000));

    try {
      transactionsRepository.applyTransaction(transaction);
      fail("Should have failed when applying to missing account");
    } catch (MissingAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account with id " + fromId + " does not exist!");
    }
  }

  @Test
  void applyTransaction_failsOnMissingToAccountId() {
    String fromId = "Id-123";
    String toId = "Id-" + System.currentTimeMillis();
    Transaction transaction = new Transaction(fromId, toId, BigDecimal.valueOf(1000));

    Account account = new Account(fromId);
    accountsRepository.createAccount(account);

    try {
      transactionsRepository.applyTransaction(transaction);
      fail("Should have failed when applying to missing account");
    } catch (MissingAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account with id " + toId + " does not exist!");
    }
  }

  @Test
  void applyTransaction_failsOnSingleAccountIdBalance() {
    String fromId = "Id-123";
    String toId = "Id-123";
    Transaction transaction = new Transaction(fromId, toId, BigDecimal.valueOf(1000));

    Account account = new Account(fromId);
    accountsRepository.createAccount(account);

    try {
      transactionsRepository.applyTransaction(transaction);
      fail("Should have failed when applying to single account");
    } catch (SingleAccountIdException ex) {
      assertThat(ex.getMessage())
          .isEqualTo("Both accounts have the same id " + toId + "!");
    }
  }

  @Test
  void applyTransaction_failsOnInvalidTransactionAmount() {
    Transaction transaction = new Transaction("Id-123", "Id-127", BigDecimal.ZERO);

    try {
      transactionsRepository.applyTransaction(transaction);
      fail("Should have failed when applying to single account");
    } catch (InvalidTransactionAmountException ex) {
      assertThat(ex.getMessage())
          .isEqualTo("Transfer amount " + BigDecimal.ZERO + " is invalid!");
    }
  }

  @Test
  void applyTransaction_failsOnNegativeAccountBalance() {
    String fromId = "Id-123";
    String toId = "Id-127";
    Transaction transaction = new Transaction(fromId, toId, BigDecimal.valueOf(1000));

    Account fromAccount = new Account(fromId);
    Account toAccount = new Account(toId);
    accountsRepository.createAccount(fromAccount);
    accountsRepository.createAccount(toAccount);

    try {
      transactionsRepository.applyTransaction(transaction);
      fail("Should have failed when applying to negative balance");
    } catch (NegativeAccountBalanceException ex) {
      assertThat(ex.getMessage())
          .isEqualTo("Account with id " + fromId + " does not support overdraft!");
    }
  }

  @Test
  void applyTransaction() {
    String fromId = "Id-123";
    String toId = "Id-127";
    Transaction transaction = new Transaction(fromId, toId, BigDecimal.valueOf(700));

    Account fromAccount = new Account(fromId, BigDecimal.valueOf(1000));
    Account toAccount = new Account(toId);
    accountsRepository.createAccount(fromAccount);
    accountsRepository.createAccount(toAccount);

    Accounts accounts = transactionsRepository.applyTransaction(transaction);

    assertThat(accounts.getFromAccount().getBalance()).isEqualTo(BigDecimal.valueOf(300));
    assertThat(accounts.getToAccount().getBalance()).isEqualTo(BigDecimal.valueOf(700));

    assertThat(accountsRepository.getAccount(fromId).getBalance())
        .isEqualTo(BigDecimal.valueOf(300));
    assertThat(accountsRepository.getAccount(toId).getBalance())
        .isEqualTo(BigDecimal.valueOf(700));
  }
}
