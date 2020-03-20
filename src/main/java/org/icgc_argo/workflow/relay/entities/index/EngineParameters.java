package org.icgc_argo.workflow.relay.entities.index;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class EngineParameters {
  String launchDir;
  String projectDir;
  String workDir;
  String revision;
  String resume;
}
