package org.icgc_argo.workflow.relay.entities.index;

import java.time.Instant;
import java.util.Map;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowDocument {

  /** Workflow run ID */
  @NonNull private String runId;

  /** Workflow session ID */
  @NonNull private String sessionId;

  /** The overall state of the workflow run, mapped to WorkflowEvent - event */
  @NonNull private WorkflowState state;

  private Map<String, Object> parameters;

  private EngineParameters engineParameters;

  /** When the command started executing */
  @NonNull private Instant startTime;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private Instant completeTime;

  /** The repository url */
  private String repository;

  /** Exit code of the program */
  private Integer exitStatus;

  /** The command line that was executed */
  @NonNull private String commandLine;

  /** A URL to retrieve standard error logs of the workflow run or task */
  private String errorReport;

  /** Did the workflow succeed */
  @NonNull private Boolean success;

  /** Workflow duration */
  private Integer duration;
}
