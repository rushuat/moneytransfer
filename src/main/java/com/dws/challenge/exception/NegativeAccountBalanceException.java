package com.dws.challenge.exception;

public class NegativeAccountBalanceException extends RuntimeException {

  public NegativeAccountBalanceException(String accountId) {
    super("Account with id " + accountId + " does not support overdraft!");
  }
}
