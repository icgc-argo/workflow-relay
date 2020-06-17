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

import static java.lang.String.format;

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
      // NEXTFLOW TASK EVENT
      log.debug(format("Processing task (Nextflow) event for runId: { %s }, runName: { %s }", event.path("runId").asText(), event.path("runName").asText()));
      taskOutput.send(MessageBuilder.withPayload(event).build());
    } else if (!event.path("metadata").path("workflow").isMissingNode()) {
      // NEXTFLOW WORKFLOW EVENT
      log.debug(format("Processing workflow (Nextflow) event for runId: { %s }, runName: { %s }", event.path("runId").asText(), event.path("runName").asText()));
      workflowOutput.send(MessageBuilder.withPayload(event).build());
    } else {
      log.error("Unhandled event: {}", event.toString());
      throw new RuntimeException("Cannot handle event, please see DLQ for event information");
    }
  }
}
