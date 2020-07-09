package org.icgc_argo.workflow.relay.util;

import static org.icgc_argo.workflow.relay.exceptions.NotFoundException.checkNotFound;

import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.entities.index.*;
import org.icgc_argo.workflow.relay.entities.nextflow.TaskEvent;
import org.icgc_argo.workflow.relay.entities.nextflow.WorkflowEvent;

/** Utility class that converts metadata POJOs to index POJOs. */
@Slf4j
@NoArgsConstructor
public class NextflowDocumentConverter {

  public static WorkflowDocument buildWorkflowDocument(@NonNull WorkflowEvent workflowEvent) {

    checkNotFound(
        workflowEvent.getMetadata() != null,
        "Cannot convert workflow event to workflow document: metadata is null.");
    checkNotFound(
        workflowEvent.getMetadata().getWorkflow() != null,
        "Cannot convert workflow event to workflow document: workflow is null.");

    val workflow = workflowEvent.getMetadata().getWorkflow();

    val engineParams =
        EngineParameters.builder()
            .revision(workflow.getRevision())
            .resume(workflow.getResume())
            .launchDir(workflow.getLaunchDir())
            .projectDir(workflow.getProjectDir())
            .workDir(workflow.getWorkDir())
            .build();

    val success = workflow.getSuccess();

    val doc =
        WorkflowDocument.builder()
            .runId(workflowEvent.getRunName())
            .sessionId(workflowEvent.getRunId())
            .state(WorkflowState.fromValueAndSuccess(workflowEvent.getEvent(), success))
            .parameters(workflowEvent.getMetadata().getParameters())
            .engineParameters(engineParams)
            .startTime(workflow.getStart().toInstant())
            .repository(workflow.getRepository())
            .commandLine(workflow.getCommandLine())
            .errorReport(workflow.getErrorReport())
            .exitStatus(workflow.getExitStatus())
            .success(success)
            .duration(workflow.getDuration());

    val completeTime = workflow.getComplete();
    if (Objects.nonNull(completeTime)) {
      doc.completeTime(completeTime.toInstant());
    }

    return doc.build();
  }

  public static TaskDocument buildTaskDocument(@NonNull TaskEvent taskEvent) {

    checkNotFound(
        taskEvent.getTrace() != null, "Cannot convert task event to task document, trace is null.");
    val trace = taskEvent.getTrace();

    return TaskDocument.builder()
        .runId(taskEvent.getRunName())
        .sessionId(taskEvent.getRunId())
        .taskId(trace.getTaskId())
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
        .rss(trace.getRss())
        .peakRss(trace.getPeakRss())
        .vmem(trace.getVmem())
        .peakVmem(trace.getPeakVmem())
        .readBytes(trace.getReadBytes())
        .writeBytes(trace.getWriteBytes())
        .build();
  }
}
