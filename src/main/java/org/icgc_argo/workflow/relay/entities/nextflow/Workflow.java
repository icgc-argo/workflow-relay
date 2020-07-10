package org.icgc_argo.workflow.relay.entities.nextflow;

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

  /** The run's launch directory (where nextflow config and cache are stored) */
  @NonNull private String launchDir;

  /** The run's project directory (where the git repo is stored) */
  @NonNull private String projectDir;

  /** The run's working directory (scratch space) */
  @NonNull private String workDir;

  /** Exit code of the program */
  private Integer exitStatus;

  /** The command line that was executed */
  @NonNull private String commandLine;

  /** A URL to retrieve standard error logs of the workflow run or task */
  private String errorReport;

  /** Was this a resume run */
  @NonNull private String resume;

  /** Did the workflow succeed */
  @NonNull private Boolean success;

  /** Workflow duration */
  private Long duration;
}
