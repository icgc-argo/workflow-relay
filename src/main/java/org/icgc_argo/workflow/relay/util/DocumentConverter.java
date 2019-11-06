package org.icgc_argo.workflow.relay.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.entities.indexing.TaskDocument;
import org.icgc_argo.workflow.relay.entities.indexing.WorkflowDocument;
import org.icgc_argo.workflow.relay.entities.metadata.TaskEvent;
import org.icgc_argo.workflow.relay.entities.metadata.WorkflowEvent;

/** Utility class that converts metadata POJOs to indexing POJOs. */
@Slf4j
@NoArgsConstructor
public class DocumentConverter {

  public static WorkflowDocument buildWorkflowDocument(WorkflowEvent workflowEvent) {
    val workflow = workflowEvent.getMetadata().getWorkflow();
    return WorkflowDocument.builder()
        .runId(workflowEvent.getRunId())
        .runName(workflowEvent.getRunName())
        .startTime(workflow.getStart())
        .completeTime(workflow.getComplete())
        .commandLine(workflow.getCommandLine())
        .errorReport(workflow.getErrorReport())
        .exitStatus(workflow.getExitStatus())
        .build();
  }

  public static TaskDocument buildTaskDocument(TaskEvent taskEvent) {
    val trace = taskEvent.getTrace();
    return TaskDocument.builder()
        .runId(taskEvent.getRunId())
        .runName(taskEvent.getRunName())
        .name(trace.getName())
        .startTime(trace.getStart())
        .completeTime(trace.getComplete())
        .script(trace.getScript())
        .exit(trace.getExit())
        .build();
  }
}
