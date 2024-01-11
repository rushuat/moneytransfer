package com.dws.challenge.config;

import com.dws.challenge.domain.Account;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChallengeConfig {

  @Bean
  public Map<String, Account> accountsInMemory() {
    return new ConcurrentHashMap<>();
  }
}
