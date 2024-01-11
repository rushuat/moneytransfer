package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transaction;
import com.dws.challenge.exception.InvalidTransactionAmountException;
import com.dws.challenge.exception.MissingAccountIdException;
import com.dws.challenge.exception.NegativeAccountBalanceException;
import com.dws.challenge.exception.SingleAccountIdException;
import com.dws.challenge.util.Accounts;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionsRepositoryInMemory implements TransactionsRepository {

  private final Map<String, Account> accounts;

  @Autowired
  public TransactionsRepositoryInMemory(Map<String, Account> accounts) {
    this.accounts = accounts;
  }

  @Override
  public Accounts applyTransaction(Transaction transaction) {
    String fromId = transaction.getFromAccountId();
    String toId = transaction.getToAccountId();
    BigDecimal transferAmount = transaction.getAmount();

    if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidTransactionAmountException(transferAmount);
    }
    Account fromAccount = accounts.get(fromId);
    if (fromAccount == null) {
      throw new MissingAccountIdException(fromId);
    }
    Account toAccount = accounts.get(toId);
    if (toAccount == null) {
      throw new MissingAccountIdException(toId);
    }
    if (fromAccount.equals(toAccount)) {
      throw new SingleAccountIdException(toId);
    }

    return applyTransaction(fromAccount.getAccountId(), toAccount.getAccountId(), transferAmount);
  }

  private Accounts applyTransaction(String fromId, String toId, BigDecimal transferAmount) {
    String smallerId = fromId.compareTo(toId) < 0 ? fromId : toId;
    String biggerId = fromId.compareTo(toId) >= 0 ? fromId : toId;
    synchronized (smallerId) {
      synchronized (biggerId) {
        Account fromAccount =
            accounts.compute(fromId, (id, account) -> {
              BigDecimal balance = account.getBalance();
              BigDecimal result = balance.subtract(transferAmount);
              if (result.compareTo(BigDecimal.ZERO) < 0) {
                throw new NegativeAccountBalanceException(id);
              }
              return new Account(id, result);
            });

        Account toAccount =
            accounts.compute(toId, (id, account) -> {
              BigDecimal balance = account.getBalance();
              BigDecimal result = balance.add(transferAmount);
              return new Account(id, result);
            });

        return new Accounts(fromAccount, toAccount);
      }
    }
  }
}
