package org.icgc_argo.workflow.relay.entities.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowEvent {

  /** workflow run ID */
  @NonNull private String runId;

  /** workflow run name */
  @NonNull private String runName;

  /** The overall state of the workflow run, mapped to WorkflowDocument's WorkflowState */
  @NonNull private String event;

  @NonNull private Metadata metadata;
}
