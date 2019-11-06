package org.icgc_argo.workflow.relay.entities.metadata;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkflowEvent {

  /** workflow run ID */
  @NonNull private String runId;

  @NonNull private Metadata metadata;

  /** workflow run name */
  @NonNull private String runName;
}
