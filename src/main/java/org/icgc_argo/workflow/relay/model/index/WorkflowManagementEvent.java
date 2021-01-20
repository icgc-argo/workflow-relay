package org.icgc_argo.workflow.relay.model.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowManagementEvent {
        @NonNull String runName;
        @NonNull String utcTime;
        @NonNull String event;
        RunParams runParams;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RunParams {
                @NonNull String workflowUrl;
                @NonNull String workflowType;
                String workflowTypeVersion;
                EngineParameters workflowEngineParams;
                Map<String, Object> workflowParams;
                @NonNull String runName;
        }
}
