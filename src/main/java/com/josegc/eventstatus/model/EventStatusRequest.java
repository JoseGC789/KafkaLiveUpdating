package com.josegc.eventstatus.model;

// File: src/main/java/com/example/eventstatus/model/EventStatusRequest.java

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventStatusRequest {
  @NotBlank private String eventId;

  private boolean status; // live or not_live
}
