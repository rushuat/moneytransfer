package com.dws.challenge.util;

import com.dws.challenge.domain.Account;
import lombok.Data;

@Data
public class Accounts {

  private final Account fromAccount;
  private final Account toAccount;
}
