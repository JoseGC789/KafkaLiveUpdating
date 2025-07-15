package com.josegc.eventstatus.service;

import com.josegc.eventstatus.client.ExternalService;
import com.josegc.eventstatus.model.EventStatusRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Service responsible for managing live event statuses, scheduling polling jobs for live events,
 * and publishing updated scores to a Kafka topic.
 *
 * <p>When an event is marked as live, a polling task is scheduled to run periodically (every 10
 * seconds). This task fetches scores from an external service and publishes them asynchronously to
 * Kafka.
 *
 * <p>When the event is no longer live, the scheduled polling task is cancelled.
 *
 * @author jose
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventStatusService {

  /** A concurrent map that holds active scheduled polling tasks for each event by ID. */
  final Map<String, ScheduledFuture<?>> eventStatusMap = new ConcurrentHashMap<>();

  /** KafkaTemplate used to publish score updates to Kafka. */
  private final KafkaTemplate<String, String> kafkaTemplate;

  /** External service used to fetch current scores for live events. */
  private final ExternalService externalService;

  /** Task scheduler used to schedule score polling jobs. */
  private TaskScheduler scheduler;

  /**
   * Initializes the task scheduler with a thread pool. This method is invoked automatically after
   * dependency injection.
   */
  @PostConstruct
  public void init() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(10);
    taskScheduler.initialize();
    this.scheduler = taskScheduler;
  }

  /**
   * Updates the status of an event. If the event is marked as live, schedules a polling task. If
   * the event is marked as not live, cancels any existing polling task.
   *
   * @param request the event status request containing the event ID and status
   */
  public void updateEventStatus(EventStatusRequest request) {
    String eventId = request.getEventId();

    if (request.isStatus()) {
      ScheduledFuture<?> scheduledFuture =
          scheduler.scheduleAtFixedRate(
              () -> fetchAndPublishScore(eventId), Duration.ofSeconds(10));
      eventStatusMap.put(eventId, scheduledFuture);
      return;
    }

    if (!eventStatusMap.containsKey(eventId)) {
      return;
    }

    ScheduledFuture<?> task = eventStatusMap.get(eventId);
    log.info(String.format("Cancelling %s %s %s", eventId, task, task.cancel(true)));
    eventStatusMap.remove(eventId);
  }

  /**
   * Fetches the current score for a given event from the external service and publishes it to the
   * Kafka topic {@code event-scores}.
   *
   * <p>If the event is no longer tracked (removed from the map), the method exits silently.
   *
   * <p>Publishing to Kafka is done asynchronously using a {@link CompletableFuture}. Any errors
   * during fetch or publish are logged accordingly.
   *
   * @param eventId the ID of the event to poll and publish
   */
  void fetchAndPublishScore(String eventId) {
    log.info("fetchAndPublishScore");
    if (!eventStatusMap.containsKey(eventId)) {
      return;
    }

    try {
      Map<String, String> rs = externalService.update(eventId);
      String score = rs.get("currentScore");
      String message = String.format("{\"eventId\":\"%s\", \"score\":\"%s\"}", eventId, score);
      log.info("Publishing to kafka");

      CompletableFuture<SendResult<String, String>> future =
          kafkaTemplate.send("event-scores", message);

      future.whenComplete(
          (result, ex) -> {
            if (ex != null) {
              log.error("Failed to publish to Kafka for eventId=" + eventId + ex);
              future.completeExceptionally(ex);
            } else {
              log.info(
                  "Successfully published to Kafka topic={%s} partition={%d} offset={%d} for eventId={%s}"
                      .formatted(
                          result.getRecordMetadata().topic(),
                          result.getRecordMetadata().partition(),
                          result.getRecordMetadata().offset(),
                          eventId));
              future.complete(result);
            }
          });
    } catch (Exception e) {
      log.error("Error during fetchAndPublishScore for eventId=" + eventId + e.getMessage());
    }
  }
}
