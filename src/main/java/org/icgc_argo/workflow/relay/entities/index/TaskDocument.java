package org.icgc_argo.workflow.relay.entities.index;

import java.util.Date;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaskDocument {

  /** Workflow run ID */
  @NonNull private String runId;

  /** Workflow run name */
  @NonNull private String runName;

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