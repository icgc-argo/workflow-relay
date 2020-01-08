package org.icgc_argo.workflow.relay.entities.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
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

  /** Task id */
  @NonNull private Integer taskId;

  /** Task name */
  @NonNull private String name;

  /** Process name */
  @NonNull private String process;

  /** Task tag */
  private String tag;

  /** Task container */
  @NonNull private String container;

  /** Attempt */
  @NonNull private Integer attempt;

  /** The overall state of the task run, mapped to Trace's "status" */
  @NonNull private TaskState state;

  /** When the command was submitted */
  @NonNull private Instant submitTime;

  /** When the command started executing */
  @NonNull private Instant startTime;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private Instant completeTime;

  /** Exit code of the program */
  @NonNull private Integer exit;

  /** The command line that was executed */
  @NonNull private String script;

  /** Task filesystem working directory */
  @NonNull private String workdir;

  /** Task cpu usage */
  private Integer cpus;

  /** Task memory usage */
  private Integer memory;

  /** Task duration (ms) */
  private Integer duration;

  /** Task real execution time (ms) */
  private Integer realtime;
}
