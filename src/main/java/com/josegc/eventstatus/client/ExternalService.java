package com.josegc.eventstatus.client;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;

public interface ExternalService {
  Map<String, String> update(@PathVariable("id") String id);
}
