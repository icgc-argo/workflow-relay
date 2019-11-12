package org.icgc_argo.workflow.relay.entities.index;

import java.util.Date;
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

  /** Workflow run name */
  @NonNull private String runName;

  /** The overall state of the workflow run, mapped to WorkflowEvent - event */
  @NonNull private WorkflowState state;

  /** When the command started executing */
  @NonNull private Date startTime;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private Date completeTime;

  /** Exit code of the program */
  private Integer exitStatus;

  /** The command line that was executed */
  @NonNull private String commandLine;

  /** A URL to retrieve standard error logs of the workflow run or task */
  private String errorReport;
}
