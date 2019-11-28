package org.icgc_argo.workflow.relay.util;

import static org.icgc_argo.workflow.relay.exceptions.NotFoundException.checkNotFound;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.entities.index.TaskDocument;
import org.icgc_argo.workflow.relay.entities.index.TaskState;
import org.icgc_argo.workflow.relay.entities.index.WorkflowDocument;
import org.icgc_argo.workflow.relay.entities.index.WorkflowState;
import org.icgc_argo.workflow.relay.entities.metadata.TaskEvent;
import org.icgc_argo.workflow.relay.entities.metadata.WorkflowEvent;

/** Utility class that converts metadata POJOs to index POJOs. */
@Slf4j
@NoArgsConstructor
public class DocumentConverter {

  public static WorkflowDocument buildWorkflowDocument(@NonNull WorkflowEvent workflowEvent) {

    checkNotFound(
        workflowEvent.getMetadata() != null,
        "Cannot convert workflow event to workflow document: metadata is null.");
    checkNotFound(
        workflowEvent.getMetadata().getWorkflow() != null,
        "Cannot convert workflow event to workflow document: workflow is null.");

    val workflow = workflowEvent.getMetadata().getWorkflow();
    return WorkflowDocument.builder()
        .runId(workflowEvent.getRunId())
        .runName(workflowEvent.getRunName())
        .state(WorkflowState.fromValue(workflowEvent.getEvent()))
        .parameters(workflowEvent.getMetadata().getParameters())
        .startTime(workflow.getStart())
        .completeTime(workflow.getComplete())
        .repository(workflow.getRepository())
        .commandLine(workflow.getCommandLine())
        .errorReport(workflow.getErrorReport())
        .exitStatus(workflow.getExitStatus())
        .build();
  }

  public static TaskDocument buildTaskDocument(@NonNull TaskEvent taskEvent) {

    checkNotFound(
        taskEvent.getTrace() != null, "Cannot convert task event to task document, trace is null.");
    val trace = taskEvent.getTrace();

    return TaskDocument.builder()
        .runId(taskEvent.getRunId())
        .runName(taskEvent.getRunName())
        .state(TaskState.fromValue(trace.getStatus()))
        .name(trace.getName())
        .startTime(trace.getStart())
        .completeTime(trace.getComplete())
        .script(trace.getScript())
        .exit(trace.getExit())
        .build();
  }
}
