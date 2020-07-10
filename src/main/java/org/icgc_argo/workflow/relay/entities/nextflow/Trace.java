package org.icgc_argo.workflow.relay.entities.nextflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trace {

  /** Task id */
  @NonNull private Integer taskId;

  /** The overall state of the task run, mapped to TaskDocument - TaskState */
  @NonNull private String status;

  /** The task or workflow name */
  @NonNull private String name;

  /** Exit code of the program */
  @NonNull private Integer exit;

  /** When the command was submitted */
  @NonNull private Instant submit;

  /** When the command started executing */
  @NonNull private Instant start;

  /** When the command stopped executing (completed, failed, or cancelled) */
  @NonNull private Instant complete;

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

  /** The cpus number request for the task execution */
  private Integer cpus;

  /** The memory request for the task execution */
  private Long memory;

  /** Time elapsed to complete since the submission */
  private Long duration;

  /** Task execution time i.e. delta between completion and start timestamp */
  private Long realtime;

  /** Real memory (resident set) size of the process. Equivalent to `ps -o rss` */
  private Long rss;

  /** Peak of real memory. This data is read from field `VmHWM` in `/proc/$pid/status file` */
  private Long peakRss;

  /** Virtual memory size of the process. Equivalent to `ps -o vsize` */
  private Long vmem;

  /** Peak of virtual memory. This data is read from field `VmPeak` in `/proc/$pid/status file` */
  private Long peakVmem;

  /**
   * Number of bytes the process directly read from disk. This data is read from file
   * `/proc/$pid/io`
   */
  private Long readBytes;

  /**
   * Number of bytes the process originally dirtied in the page-cache (assuming they will go to disk
   * later). This data is read from file `/proc/$pid/io`
   */
  private Long writeBytes;
}
