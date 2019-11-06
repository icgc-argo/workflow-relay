package org.icgc_argo.workflow.relay.entities.metadata;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Metadata {

  @NonNull private Workflow workflow;
}
