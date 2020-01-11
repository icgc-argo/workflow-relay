package org.icgc_argo.workflow.relay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.config.stream.WebLogStream;
import org.icgc_argo.workflow.relay.config.weblog.SanitizeProperties;
import org.icgc_argo.workflow.relay.util.Sanitize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@Profile("weblog")
@EnableBinding(WebLogStream.class)
public class WebLogService {

  private WebLogStream webLogStream;
  private SanitizeProperties sanitizeProperties;

  @Autowired
  public WebLogService(WebLogStream webLogStream, SanitizeProperties sanitizeProperties) {
    this.webLogStream = webLogStream;
    this.sanitizeProperties = sanitizeProperties;
    log.info("Paths to Sanitize {}", sanitizeProperties.getPaths().toString());
  }

  public void handleEvent(@NonNull JsonNode event) {
    log.info("handling event: {}", event);
    sanitizeProperties.getPaths().forEach(path -> {
      Sanitize.sanitize(path, event);
    });
    webLogStream.webLogOutput().send(MessageBuilder.withPayload(event).build());
  }
}
