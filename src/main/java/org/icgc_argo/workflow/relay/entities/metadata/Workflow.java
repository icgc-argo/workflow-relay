package org.icgc_argo.workflow.relay.entities.metadata;

import java.util.Date;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Workflow {

  /** When the command started executing */
  @NonNull private Date start;

  /** When the command stopped executing (completed, failed, or cancelled) */
  @NonNull private Date complete;

  /** Exit code of the program */
  private Integer exitStatus;

  /** The command line that was executed */
  @NonNull private String commandLine;

  /** A URL to retrieve standard error logs of the workflow run or task */
  private String errorReport;
}
