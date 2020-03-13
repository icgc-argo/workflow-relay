package org.icgc_argo.workflow.relay.entities.index;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WorkflowState {
  RUNNING("RUNNING"),

  COMPLETE("COMPLETE"),

  EXECUTOR_ERROR("EXECUTOR_ERROR"),

  UNKNOWN("UNKNOWN");

  @NonNull private final String value;

  public static WorkflowState fromValueAndSuccess(@NonNull String text, @NonNull boolean success) {
    if (text.equalsIgnoreCase("started")) {
      return WorkflowState.RUNNING;
    } else if (text.equalsIgnoreCase("completed") && success) {
      return WorkflowState.COMPLETE;
    } else if (text.equalsIgnoreCase("completed") && !success) {
      return WorkflowState.EXECUTOR_ERROR;
    } else return WorkflowState.UNKNOWN;
  }

  @Override
  public String toString() {
    return value;
  }
}
