package com.dws.challenge.exception;

public class SingleAccountIdException extends RuntimeException {

  public SingleAccountIdException(String accountId) {
    super("Both accounts have the same id " + accountId + "!");
  }
}
