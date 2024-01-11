package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.Transaction;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.web.TransactionsController;
import java.math.BigDecimal;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TransactionsConcurrencyTest {

  private static final int ACCOUNT_NUMBER = 10;
  private static final int ACCOUNT_BALANCE = 1000;
  private static final int TRANSACTION_NUMBER = 1_000_000;

  private final Random fromRandom = new Random();
  private final Random toRandom = new Random();
  private final Random amountRandom = new Random();

  private final Function<Integer, String> strId = i -> "Id" + i;

  private final Supplier<String> fromId = () -> strId.apply(fromRandom.nextInt(ACCOUNT_NUMBER));
  private final Supplier<String> toId = () -> strId.apply(toRandom.nextInt(ACCOUNT_NUMBER));
  private final Supplier<Integer> amount = () -> amountRandom.nextInt(ACCOUNT_NUMBER);

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private TransactionsController transactionsController;

  private Transaction generateTransaction() {
    return new Transaction(fromId.get(), toId.get(), BigDecimal.valueOf(amount.get()));
  }

  private BigDecimal totalBalance() {
    return
        IntStream.range(0, ACCOUNT_NUMBER)
            .mapToObj(strId::apply)
            .map(accountsService::getAccount)
            .map(Account::getBalance)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
  }

  @BeforeEach
  void prepareRepository() {
    IntStream.range(0, ACCOUNT_NUMBER)
        .mapToObj(strId::apply)
        .map(id -> new Account(id, BigDecimal.valueOf(ACCOUNT_BALANCE)))
        .forEach(accountsService::createAccount);
  }

  @Disabled("Concurrency basic testing is disabled!")
  @Test
  void applyTransactions() {
    BigDecimal totalBalance = totalBalance();

    Stream.generate(this::generateTransaction)
        .limit(TRANSACTION_NUMBER)
        .parallel()
        .forEach(transactionsController::applyTransaction);

    assertThat(totalBalance()).isEqualTo(totalBalance);
  }
}
