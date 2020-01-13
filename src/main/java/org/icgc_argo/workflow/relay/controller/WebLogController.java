package org.icgc_argo.workflow.relay.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.icgc_argo.workflow.relay.service.WebLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/")
@Profile("weblog")
public class WebLogController {

  private WebLogService webLogService;

  @Autowired
  public WebLogController(WebLogService webLogService) {
    this.webLogService = webLogService;
  }

  @SneakyThrows
  @PostMapping(consumes = "application/json")
  public Mono<ResponseEntity<Boolean>> consumeEvent(@RequestBody JsonNode event) {
    // Use hashcode to see if identical events are being submitted
    log.debug("Received event with hashcode: {}", event.hashCode());
    webLogService.handleEvent(event);
    return Mono.just(new ResponseEntity<>(null, HttpStatus.OK));
  }
}
