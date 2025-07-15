package com.josegc.eventstatus.controller;

import com.josegc.eventstatus.model.EventStatusRequest;
import com.josegc.eventstatus.service.EventStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes endpoints to update the live/not-live status of sports events.
 *
 * <p>This controller accepts POST requests containing event IDs and their status, delegating the
 * logic to the {@link EventStatusService} which handles the scheduling and cancellation of event
 * polling jobs.
 *
 * @author jose
 * @since 1.0
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventStatusController {

  /** Service responsible for handling event status updates and job scheduling. */
  private final EventStatusService eventStatusService;

  /**
   * Handles HTTP POST requests to update the status of a sports event.
   *
   * <p>If the event is marked as {@code status = true}, a scheduled polling job will be created. If
   * {@code status = false}, any existing polling job for the event will be cancelled.
   *
   * @param request the event status request payload, containing an event ID and status flag
   * @return HTTP 200 OK if the update was accepted
   * @throws jakarta.validation.ConstraintViolationException if the request is invalid
   */
  @PostMapping("/status")
  public ResponseEntity<Void> updateEventStatus(@Valid @RequestBody EventStatusRequest request) {
    log.info(
        "Received status update for eventId={} with status %s %s"
            .formatted(request.getEventId(), request.isStatus()));
    eventStatusService.updateEventStatus(request);
    return ResponseEntity.ok().build();
  }
}
