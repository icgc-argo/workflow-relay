package org.icgc_argo.workflow.relay.util;

import static org.icgc_argo.workflow.relay.exceptions.NotFoundException.checkNotFound;

import java.time.Instant;
import java.util.Objects;
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
    val doc =
        WorkflowDocument.builder()
            .runId(workflowEvent.getRunId())
            .runName(workflowEvent.getRunName())
            .state(WorkflowState.fromValue(workflowEvent.getEvent()))
            .parameters(workflowEvent.getMetadata().getParameters())
            .startTime(Instant.from(workflow.getStart().toInstant()))
            .repository(workflow.getRepository())
            .revision(workflow.getRevision())
            .commandLine(workflow.getCommandLine())
            .errorReport(workflow.getErrorReport())
            .exitStatus(workflow.getExitStatus())
            .resume(workflow.getResume())
            .success(workflow.getSuccess())
            .duration(workflow.getDuration());

    val completeTime = workflow.getComplete();
    if (Objects.nonNull(completeTime)) {
      doc.completeTime(Instant.from(completeTime.toInstant()));
    }

    return doc.build();
  }

  public static TaskDocument buildTaskDocument(@NonNull TaskEvent taskEvent) {

    checkNotFound(
        taskEvent.getTrace() != null, "Cannot convert task event to task document, trace is null.");
    val trace = taskEvent.getTrace();

    return TaskDocument.builder()
        .runId(taskEvent.getRunId())
        .runName(taskEvent.getRunName())
        .taskId(trace.getTask_id())
        .name(trace.getName())
        .process(trace.getProcess())
        .tag(trace.getTag())
        .container(trace.getContainer())
        .attempt(trace.getAttempt())
        .state(TaskState.fromValue(trace.getStatus()))
        .submitTime(trace.getSubmit())
        .startTime(trace.getStart())
        .completeTime(trace.getComplete())
        .exit(trace.getExit())
        .script(trace.getScript())
        .workdir(trace.getWorkdir())
        .cpus(trace.getCpus())
        .memory(trace.getMemory())
        .duration(trace.getDuration())
        .realtime(trace.getRealtime())
        .build();
  }
}
