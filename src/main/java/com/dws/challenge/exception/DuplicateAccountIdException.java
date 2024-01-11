package com.dws.challenge.exception;

public class DuplicateAccountIdException extends RuntimeException {

  public DuplicateAccountIdException(String accountId) {
    super("Account with id " + accountId + " already exists!");
  }
}
