package org.icgc_argo.workflow.relay.entities.index;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TaskState {
  RUNNING("RUNNING"),

  COMPLETE("COMPLETE"),

  QUEUED("QUEUED"),

  UNKNOWN("UNKNOWN"),

  EXECUTOR_ERROR("EXECUTOR_ERROR");

  @NonNull private final String value;

  public static TaskState fromValue(@NonNull String text) {
    if (text.equalsIgnoreCase("RUNNING")) {
      return TaskState.RUNNING;
    } else if (text.equalsIgnoreCase("SUBMITTED")) {
      return TaskState.QUEUED;
    } else if (text.equalsIgnoreCase("COMPLETED")) {
      return TaskState.COMPLETE;
    } else if (text.equalsIgnoreCase("FAILED")) {
      return TaskState.EXECUTOR_ERROR;
    } else return TaskState.UNKNOWN;
  }

  @Override
  public String toString() {
    return value;
  }
}
