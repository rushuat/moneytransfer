package com.dws.challenge.repository;

import com.dws.challenge.domain.Transaction;
import com.dws.challenge.util.Accounts;

public interface TransactionsRepository {

  Accounts applyTransaction(Transaction transaction);
}
