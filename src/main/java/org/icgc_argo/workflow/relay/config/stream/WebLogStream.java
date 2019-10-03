package org.icgc_argo.workflow.relay.config.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface WebLogStream {

  String WEBLOG = "weblog";

  @Output(WEBLOG)
  MessageChannel webLogOutput();
}
