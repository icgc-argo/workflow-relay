package org.icgc_argo.workflow.relay.config.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface SplitStream {

  String WEBLOG = "weblogout";
  String WORKFLOW = "workflow";
  String TASK = "task";

  @Input(WEBLOG)
  SubscribableChannel webLogOutInput();

  @Output(WORKFLOW)
  MessageChannel workflowOutput();

  @Output(TASK)
  MessageChannel taskOutput();

}
