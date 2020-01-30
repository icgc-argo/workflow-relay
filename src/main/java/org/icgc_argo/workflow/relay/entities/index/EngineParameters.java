package org.icgc_argo.workflow.relay.entities.index;

import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EngineParameters {
  String workDir;
  String revision;
  String resume;
}
