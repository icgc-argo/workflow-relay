package org.icgc_argo.workflow.relay.config.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface GraphLogStream {
  String INFODEBUG = "graphlog_info_debug_index";
  String WARNINGERROR = "graphlog_error_warning_index";

  @Input(INFODEBUG)
  SubscribableChannel debugInfoOutput();

  @Input(WARNINGERROR)
  SubscribableChannel errorWarningOutput();
}
