package org.icgc_argo.workflow.relay.entities.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDocument {

  /** Workflow run ID */
  @NonNull private String runId;

  /** Workflow run name */
  @NonNull private String runName;

  /** The overall state of the task run, mapped to Trace's "status" */
  @NonNull private TaskState state;

  /** When the command started executing */
  @NonNull private Date startTime;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private Date completeTime;

  /** Exit code of the program */
  @NonNull private Integer exit;

  /** Workflow run name */
  @NonNull private String name;

  /** The command line that was executed */
  @NonNull private String script;
}
