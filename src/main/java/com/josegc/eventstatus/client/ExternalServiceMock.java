package com.josegc.eventstatus.client;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ExternalServiceMock implements ExternalService {

  @Override
  public Map<String, String> update(String id) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int randomNumber = random.nextInt(10);
    return Map.of(
        "eventId",
        id,
        "score",
        String.valueOf(randomNumber),
        "pop",
        String.valueOf(randomNumber == 5));
  }
}
