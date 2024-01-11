package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.dws.challenge.domain.Transaction;
import com.dws.challenge.exception.InvalidTransactionAmountException;
import com.dws.challenge.exception.MissingAccountIdException;
import com.dws.challenge.exception.NegativeAccountBalanceException;
import com.dws.challenge.exception.SingleAccountIdException;
import com.dws.challenge.service.TransactionsService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransactionsControllerTest {

  private MockMvc mockMvc;

  @Captor
  private ArgumentCaptor<Transaction> transactionCaptor;
  @MockBean
  private TransactionsService transactionsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    mockMvc = webAppContextSetup(webApplicationContext).build();
  }

  @ParameterizedTest
  @CsvSource({"'',Id-127,700", "Id-123,'',700", "Id-123,Id-127,-700",})
  void applyTransactionInvalidParameters(
      String fromId, String toId, String amount) throws Exception {
    mockMvc.perform(put("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
        .content(
            "{\"fromAccountId\":\"" + fromId
                + "\",\"toAccountId\":\"" + toId
                + "\",\"amount\":" + amount + "}"))
        .andExpect(status().isBadRequest());

    verify(transactionsService, times(0)).applyTransaction(any());
  }

  @Test
  void applyTransactionMissingAccountId() throws Exception {
    MissingAccountIdException maie = new MissingAccountIdException("Id-127");
    doThrow(maie).when(transactionsService).applyTransaction(transactionCaptor.capture());

    mockMvc.perform(put("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
        .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-127\",\"amount\":700}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(maie.getMessage()));

    verify(transactionsService, times(1)).applyTransaction(any());

    Transaction transaction = transactionCaptor.getValue();

    assertThat(transaction.getFromAccountId()).isEqualTo("Id-123");
    assertThat(transaction.getToAccountId()).isEqualTo("Id-127");
    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(700));
  }

  @Test
  void applyTransactionSingleAccountId() throws Exception {
    SingleAccountIdException saie = new SingleAccountIdException("Id-123");
    doThrow(saie).when(transactionsService).applyTransaction(transactionCaptor.capture());

    mockMvc.perform(put("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
        .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-123\",\"amount\":700}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(saie.getMessage()));

    verify(transactionsService, times(1)).applyTransaction(any());

    Transaction transaction = transactionCaptor.getValue();

    assertThat(transaction.getFromAccountId()).isEqualTo("Id-123");
    assertThat(transaction.getToAccountId()).isEqualTo("Id-123");
    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(700));
  }

  @Test
  void applyTransactionInvalidTransactionAmount() throws Exception {
    InvalidTransactionAmountException itae = new InvalidTransactionAmountException(BigDecimal.ZERO);
    doThrow(itae).when(transactionsService).applyTransaction(transactionCaptor.capture());

    mockMvc.perform(put("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
        .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-127\",\"amount\":0}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(itae.getMessage()));

    verify(transactionsService, times(1)).applyTransaction(any());

    Transaction transaction = transactionCaptor.getValue();

    assertThat(transaction.getFromAccountId()).isEqualTo("Id-123");
    assertThat(transaction.getToAccountId()).isEqualTo("Id-127");
    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  void applyTransactionNegativeAccountBalance() throws Exception {
    NegativeAccountBalanceException nabe = new NegativeAccountBalanceException("Id-123");
    doThrow(nabe).when(transactionsService).applyTransaction(transactionCaptor.capture());

    mockMvc.perform(put("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
        .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-127\",\"amount\":700}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(nabe.getMessage()));

    verify(transactionsService, times(1)).applyTransaction(any());

    Transaction transaction = transactionCaptor.getValue();

    assertThat(transaction.getFromAccountId()).isEqualTo("Id-123");
    assertThat(transaction.getToAccountId()).isEqualTo("Id-127");
    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(700));
  }

  @Test
  void applyTransaction() throws Exception {
    doNothing().when(transactionsService).applyTransaction(transactionCaptor.capture());

    mockMvc.perform(put("/v1/transactions").contentType(MediaType.APPLICATION_JSON)
        .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-127\",\"amount\":700}"))
        .andExpect(status().isOk());

    verify(transactionsService, times(1)).applyTransaction(any());

    Transaction transaction = transactionCaptor.getValue();

    assertThat(transaction.getFromAccountId()).isEqualTo("Id-123");
    assertThat(transaction.getToAccountId()).isEqualTo("Id-127");
    assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(700));
  }
}
