package org.icgc_argo.workflow.relay.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.config.stream.WebLogStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/")
@Profile("weblog")
@EnableBinding(WebLogStream.class)
public class WebLogController {

  private WebLogStream webLogStream;

  @Autowired
  public WebLogController(WebLogStream webLogStream) {
    this.webLogStream = webLogStream;
  }

  @SneakyThrows
  @PostMapping(consumes = "application/json")
  public Mono<ResponseEntity<Boolean>> consumeEvent(@RequestBody JsonNode event) {
    webLogStream.webLogOutput().send(MessageBuilder.withPayload(event).build());
    return Mono.just(new ResponseEntity<>(null, HttpStatus.OK));
  }

}
