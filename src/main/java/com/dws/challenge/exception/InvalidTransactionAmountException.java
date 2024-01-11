package com.dws.challenge.exception;

import java.math.BigDecimal;

public class InvalidTransactionAmountException extends RuntimeException {

  public InvalidTransactionAmountException(BigDecimal amount) {
    super("Transfer amount " + amount + " is invalid!");
  }
}
