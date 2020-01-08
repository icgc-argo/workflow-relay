package org.icgc_argo.workflow.relay.config.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface IndexStream {

  String WORKFLOW = "workflowindex";
  String TASK = "taskindex";

  @Input(WORKFLOW)
  SubscribableChannel workflowOutput();

  @Input(TASK)
  SubscribableChannel taskOutput();
}
