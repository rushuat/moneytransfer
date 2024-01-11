package com.dws.challenge.exception;

public class MissingAccountIdException extends RuntimeException {

  public MissingAccountIdException(String accountId) {
    super("Account with id " + accountId + " does not exist!");
  }
}
