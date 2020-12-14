package org.icgc_argo.workflow.relay.config.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface GraphLogStream {
    String INFODEBUG = "graphlog_infoDebug_index";
    String WARNINGERROR = "graphlog_errorWarning_index";

    @Input(INFODEBUG)
    SubscribableChannel debugInfoOutput();

    @Input(WARNINGERROR)
    SubscribableChannel errorWarningOutput();
}
