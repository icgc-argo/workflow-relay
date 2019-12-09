package org.icgc_argo.workflow.relay.entities.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trace {

  /** Task id */
  @NonNull private Integer task_id;

  /** The overall state of the task run, mapped to TaskDocument - TaskState */
  @NonNull private String status;

  /** The task or workflow name */
  @NonNull private String name;

  /** Exit code of the program */
  @NonNull private Integer exit;

  /** When the command was submitted */
  @NonNull private Date submit;

  /** When the command started executing */
  @NonNull private Date start;

  /** When the command stopped executing (completed, failed, or cancelled) */
  @NonNull private Date complete;

  /** The process name */
  @NonNull private String process;

  /** Task tag */
  private String tag;

  /** Task container */
  @NonNull private String container;

  /** Attempt */
  @NonNull private Integer attempt;

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
