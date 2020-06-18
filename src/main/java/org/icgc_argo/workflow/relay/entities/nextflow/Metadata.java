package org.icgc_argo.workflow.relay.entities.nextflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {

  /** The workflow run parameters including input and output file locations * */
  @NonNull private Map<String, Object> parameters;

  @NonNull private Workflow workflow;
}
