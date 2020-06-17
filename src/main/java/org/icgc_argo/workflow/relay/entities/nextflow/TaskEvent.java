package org.icgc_argo.workflow.relay.entities.nextflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskEvent {

  @NonNull private Trace trace;

  /** Workflow run ID */
  @NonNull private String runId;

  /** Workflow run name */
  @NonNull private String runName;
}
