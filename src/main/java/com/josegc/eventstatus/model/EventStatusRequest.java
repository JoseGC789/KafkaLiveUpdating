package com.josegc.eventstatus.model;

import jakarta.validation.constraints.NotBlank;

/**
 * @param status live or not_live
 */
public record EventStatusRequest(@NotBlank String eventId, boolean status) {}
