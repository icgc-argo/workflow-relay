package org.icgc_argo.workflow.relay.entities.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflow {

  /** When the command started executing */
  @NonNull private OffsetDateTime start;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private OffsetDateTime complete;

  /** The repository url */
  private String repository;

  /** The repository release/tag/branch */
  private String revision;

  /** Exit code of the program */
  private Integer exitStatus;

  /** The command line that was executed */
  @NonNull private String commandLine;

  /** A URL to retrieve standard error logs of the workflow run or task */
  private String errorReport;

  /** Was this a resume run */
  @NonNull private Boolean resume;

  /** Did the workflow succeed */
  @NonNull private Boolean success;

  /** Workflow duration */
  private Integer duration;
}
