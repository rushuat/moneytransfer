package com.dws.challenge.web;

import com.dws.challenge.domain.Transaction;
import com.dws.challenge.exception.InvalidTransactionAmountException;
import com.dws.challenge.exception.MissingAccountIdException;
import com.dws.challenge.exception.NegativeAccountBalanceException;
import com.dws.challenge.exception.SingleAccountIdException;
import com.dws.challenge.service.TransactionsService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
@Slf4j
public class TransactionsController {

  private final TransactionsService transactionsService;

  @Autowired
  public TransactionsController(TransactionsService transactionsService) {
    this.transactionsService = transactionsService;
  }

  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> applyTransaction(@RequestBody @Valid Transaction transaction) {
    log.info("Applying transaction {}", transaction);

    try {
      transactionsService.applyTransaction(transaction);
    } catch (MissingAccountIdException | SingleAccountIdException
        | InvalidTransactionAmountException | NegativeAccountBalanceException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
