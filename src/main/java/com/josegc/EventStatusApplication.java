/*
 * This is a complete Spring Boot microservice implementation based on the requirements provided.
 * It supports receiving event status updates, scheduling periodic polling for live events,
 * fetching scores from a mocked API, and publishing them to Kafka.
 */
package com.josegc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventStatusApplication {
  public static void main(String[] args) {
    SpringApplication.run(EventStatusApplication.class, args);
  }
}
