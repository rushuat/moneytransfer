package com.dws.challenge.service;

import com.dws.challenge.domain.Transaction;
import com.dws.challenge.repository.TransactionsRepository;
import com.dws.challenge.util.Accounts;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionsService {

  private final TransactionsRepository transactionsRepository;
  private final NotificationService notificationService;

  @Autowired
  public TransactionsService(
      TransactionsRepository transactionsRepository,
      NotificationService notificationService) {
    this.transactionsRepository = transactionsRepository;
    this.notificationService = notificationService;
  }

  public void applyTransaction(Transaction transaction) {
    Accounts accounts =
        transactionsRepository.applyTransaction(transaction);

    try {
      String message =
          MessageFormat.format("transfer {0} to {1}",
              transaction.getAmount(), transaction.getToAccountId());
      notificationService.notifyAboutTransfer(accounts.getFromAccount(), message);
    } catch (Exception e) {
      log.warn("Sender with id " + transaction.getFromAccountId()
          + " has not been properly notified", e);
    }

    try {
      String message =
          MessageFormat.format("receive {0} from {1}",
              transaction.getAmount(), transaction.getFromAccountId());
      notificationService.notifyAboutTransfer(accounts.getToAccount(), message);
    } catch (Exception e) {
      log.warn("Receiver with id " + transaction.getToAccountId()
          + " has not been properly notified", e);
    }
  }
}
