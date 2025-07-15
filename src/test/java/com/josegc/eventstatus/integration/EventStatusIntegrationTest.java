package com.josegc.eventstatus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josegc.eventstatus.client.ExternalService;
import com.josegc.eventstatus.model.EventStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=mock-kafka:9092"})
class EventStatusIntegrationTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private KafkaTemplate<String, String> kafkaTemplate;

  @Test
  void shouldSchedulePollingAndPublishToKafka() throws Exception {
    // Arrange
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

    String eventId = "test-event-001";


    CompletableFuture<SendResult<String, String>> future =
        CompletableFuture.completedFuture(mock(SendResult.class));
    when(kafkaTemplate.send(eq("event-scores"), contains(eventId))).thenReturn(future);

    EventStatusRequest request = new EventStatusRequest(eventId, true);
    String body = objectMapper.writeValueAsString(request);

    // Act
    mockMvc
        .perform(post("/events/status").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());

    // Wait for the scheduled job to trigger
    Thread.sleep(11_000);

    verify(kafkaTemplate, atLeastOnce()).send(eq("event-scores"), contains(eventId));
  }
}
