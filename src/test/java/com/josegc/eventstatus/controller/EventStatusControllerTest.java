package com.josegc.eventstatus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josegc.eventstatus.model.EventStatusRequest;
import com.josegc.eventstatus.service.EventStatusService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventStatusController.class)
class EventStatusControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private EventStatusService eventStatusService;

  @Test
  void testUpdateEventStatus_returnsOkAndCallsService() throws Exception {
    EventStatusRequest request = new EventStatusRequest("event-1", true);

    mockMvc
        .perform(
            post("/events/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    Mockito.verify(eventStatusService, times(1)).updateEventStatus(Mockito.any());
  }
}
