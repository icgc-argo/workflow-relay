package org.icgc_argo.workflow.relay.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.val;
import org.icgc_argo.workflow.relay.config.stream.WebLogStream;
import org.icgc_argo.workflow.relay.config.weblog.SanitizeProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;

@Profile("weblog")
@EnableBinding(WebLogStream.class)
public class WebLogService {

  private WebLogStream webLogStream;
  private SanitizeProperties sanitizeProperties;

  @Autowired
  public WebLogService(WebLogStream webLogStream, SanitizeProperties sanitizeProperties) {
    this.webLogStream = webLogStream;
    this.sanitizeProperties = sanitizeProperties;
  }

  public void handleEvent(@NonNull JsonNode event) {
    sanitizeProperties.getPaths().forEach(path -> {
      val node = event.path(path);
      if (node.isValueNode()) {
        // do stuff
      }
    });
    webLogStream.webLogOutput().send(MessageBuilder.withPayload(event).build());
  }
}
