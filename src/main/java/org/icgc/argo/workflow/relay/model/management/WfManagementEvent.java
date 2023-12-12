package org.icgc.argo.workflow.relay.model.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.*;
import org.icgc.argo.workflow.relay.model.index.EngineParameters;
import org.icgc.argo.workflow.relay.model.index.WorkflowState;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WfManagementEvent {
  @NonNull private String runId;
  @NonNull private WorkflowState event;
  @NonNull private String utcTime;
  // TODO - workflowUrl needs to be @NonNull, its missing it now because currently only INITIALIZING
  // events have this info available
  private String workflowUrl;
  private String workflowType;
  private String workflowTypeVersion;
  private Map<String, Object> workflowParams;
  private EngineParameters workflowEngineParams;
}
