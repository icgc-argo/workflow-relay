package org.icgc_argo.workflow.relay.model.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.icgc_argo.workflow.relay.model.index.EngineParameters;
import org.icgc_argo.workflow.relay.model.index.WorkflowState;

import java.util.Map;

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
        @NonNull private String workflowUrl;
        private String workflowType;
        private String workflowTypeVersion;
        private Map<String, Object> workflowParams;
        private EngineParameters workflowEngineParams;
}
