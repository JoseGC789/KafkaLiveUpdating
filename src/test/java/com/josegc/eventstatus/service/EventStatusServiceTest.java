package com.josegc.eventstatus.service;

import com.josegc.eventstatus.client.ExternalService;
import com.josegc.eventstatus.model.EventStatusRequest;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventStatusServiceTest {

  @Mock private KafkaTemplate<String, String> kafkaTemplate;

  @Mock private ExternalService externalService;

  @InjectMocks private EventStatusService eventStatusService;

  @BeforeEach
  void setup() {
    eventStatusService.init(); // Initializes scheduler
  }

  @Test
  void testScheduleLiveEvent() {
    EventStatusRequest request = new EventStatusRequest("match-1", true);
    eventStatusService.updateEventStatus(request);
    assertTrue(eventStatusService.eventStatusMap.containsKey("match-1"));
  }

  @Test
  void testCancelScheduledEvent() {
    // First, schedule it
    EventStatusRequest live = new EventStatusRequest("match-2", true);
    eventStatusService.updateEventStatus(live);
    assertTrue(eventStatusService.eventStatusMap.containsKey("match-2"));
    // Now, cancel it
    EventStatusRequest notLive = new EventStatusRequest("match-2", false);
    eventStatusService.updateEventStatus(notLive);
    assertFalse(eventStatusService.eventStatusMap.containsKey("match-2"));
  }

  @Test
  void testCancelNonScheduledEvent_doesNothing() {
    EventStatusRequest notLive = new EventStatusRequest("nonexistent", false);
    assertDoesNotThrow(() -> eventStatusService.updateEventStatus(notLive));
  }

  @Test
  void testFetchAndPublishScoreJustOnce_successful() {
    String eventId = "match-3";
    String expectedMessage = "{\"eventId\":\"match-3\", \"score\":\"2:1\"}";

    eventStatusService.eventStatusMap.put(eventId, mock(ScheduledFuture.class));

    when(externalService.update(eventId)).thenReturn(Map.of("currentScore", "2:1"));

    SendResult<String, String> sendResult = mock(SendResult.class);
    RecordMetadata metadata = mock(RecordMetadata.class);
    when(metadata.topic()).thenReturn("event-scores");
    when(metadata.partition()).thenReturn(0);
    when(metadata.offset()).thenReturn(42L);
    when(sendResult.getRecordMetadata()).thenReturn(metadata);

    CompletableFuture<SendResult<String, String>> kafkaFuture = new CompletableFuture<>();
    kafkaFuture.complete(sendResult);
    when(kafkaTemplate.send(eq("event-scores"), eq(expectedMessage))).thenReturn(kafkaFuture);

    // Act
    eventStatusService.updateEventStatus(new EventStatusRequest(eventId, true));
    eventStatusService.fetchAndPublishScore(eventId); // Call manually

    // Assert
    verify(kafkaTemplate, times(1)).send(eq("event-scores"), eq(expectedMessage));
  }

  @Test
  void testFetchAndPublishScore_exceptionInExternalService() {
    String eventId = "match-4";
    eventStatusService.eventStatusMap.put(eventId, mock(ScheduledFuture.class));

    when(externalService.update(eventId)).thenThrow(new RuntimeException("Boom"));

    // Act (should not crash)
    eventStatusService.fetchAndPublishScore(eventId);

    // Expect no Kafka send attempt
    verify(kafkaTemplate, never()).send(any(), any());
  }
}
