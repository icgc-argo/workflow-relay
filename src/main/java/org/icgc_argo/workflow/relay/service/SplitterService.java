package org.icgc_argo.workflow.relay.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.icgc_argo.workflow.relay.config.stream.SplitStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Profile("splitter")
@Slf4j
@Service
@EnableBinding(SplitStream.class)
public class SplitterService {

  private MessageChannel workflowOutput;
  private MessageChannel taskOutput;

  @Autowired
  public SplitterService(SplitStream splitStream) {
    this.workflowOutput = splitStream.workflowOutput();
    this.taskOutput = splitStream.taskOutput();
  }

  @StreamListener(SplitStream.WEBLOG)
  public void split(JsonNode event) {
    if (event.has("trace")) {
      log.debug("Processing task event");
      taskOutput.send(MessageBuilder.withPayload(event).build());
    } else if (!event.path("metadata").path("workflow").isMissingNode()) {
      log.debug("Processing workflow event");
      workflowOutput.send(MessageBuilder.withPayload(event).build());
    } else {
      // TODO Use error topic
      log.error("Unhandled event: {}", event.toString());
    }
  }
}