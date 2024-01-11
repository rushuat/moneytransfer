package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transaction;
import com.dws.challenge.repository.TransactionsRepository;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransactionsService;
import com.dws.challenge.util.Accounts;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionsServiceTest {

  private final Account fromAccount = new Account("Id-123", BigDecimal.valueOf(300));
  private final Account toAccount = new Account("Id-127", BigDecimal.valueOf(700));
  private final Transaction transaction =
      new Transaction(
          fromAccount.getAccountId(), toAccount.getAccountId(), BigDecimal.valueOf(700)
      );

  @Captor
  private ArgumentCaptor<Account> accountCaptor;
  @Captor
  private ArgumentCaptor<String> messageCaptor;

  @MockBean
  private TransactionsRepository transactionsRepository;
  @MockBean
  private NotificationService notificationService;

  @Autowired
  private TransactionsService transactionsService;

  @BeforeEach
  void prepareMockBeans() {
    doReturn(new Accounts(fromAccount, toAccount)).when(transactionsRepository)
        .applyTransaction(transaction);
    doNothing().when(notificationService)
        .notifyAboutTransfer(accountCaptor.capture(), messageCaptor.capture());
  }

  @Test
  void applyTransaction() {
    transactionsService.applyTransaction(transaction);

    verify(transactionsRepository, times(1)).applyTransaction(transaction);
    verify(notificationService, times(1)).notifyAboutTransfer(eq(fromAccount), anyString());
    verify(notificationService, times(1)).notifyAboutTransfer(eq(toAccount), anyString());

    List<Account> accounts = accountCaptor.getAllValues();
    List<String> messages = messageCaptor.getAllValues();

    assertThat(accounts.size()).isEqualTo(2);
    assertThat(messages.size()).isEqualTo(2);

    assertThat(accounts).contains(fromAccount);
    assertThat(accounts).contains(toAccount);

    assertThat(messages).contains(
        "transfer " + transaction.getAmount()
            + " to " + transaction.getToAccountId());
    assertThat(messages).contains(
        "receive " + transaction.getAmount()
            + " from " + transaction.getFromAccountId());
  }
}
