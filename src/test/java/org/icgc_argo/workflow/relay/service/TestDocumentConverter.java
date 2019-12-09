package org.icgc_argo.workflow.relay.service;

import static org.icgc_argo.workflow.relay.util.Fixture.loadJsonFixture;
import static org.icgc_argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.entities.index.TaskState;
import org.icgc_argo.workflow.relay.entities.index.WorkflowState;
import org.icgc_argo.workflow.relay.entities.metadata.TaskEvent;
import org.icgc_argo.workflow.relay.entities.metadata.WorkflowEvent;
import org.icgc_argo.workflow.relay.util.DocumentConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
public class TestDocumentConverter {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .registerModule(getOffsetDateTimeModule());

  @Test
  public void testConvertWorkflowJson() {
    val workflowEvent =
        loadJsonFixture(this.getClass(), "workflow_event.json", WorkflowEvent.class, MAPPER);

    val expected = workflowEvent.getMetadata().getWorkflow();
    val doc = DocumentConverter.buildWorkflowDocument(workflowEvent);

    assertEquals(workflowEvent.getRunId(), doc.getRunId());
    assertEquals(workflowEvent.getRunName(), doc.getRunName());
    assertEquals(doc.getState(), WorkflowState.COMPLETE);
    assertEquals(workflowEvent.getMetadata().getParameters(), doc.getParameters());
    assertEquals(
        expected.getStart().toInstant(),
        doc.getStartTime());
    assertEquals(
        expected.getComplete().toInstant(),
        doc.getCompleteTime());
    assertEquals(expected.getRepository(), doc.getRepository());
    assertEquals(expected.getErrorReport(), doc.getErrorReport());
    assertEquals(expected.getExitStatus(), doc.getExitStatus());
    assertEquals(expected.getCommandLine(), doc.getCommandLine());
  }

  @Test
  public void testLoadTaskJson() {
    val taskEvent = loadJsonFixture(this.getClass(), "task_event.json", TaskEvent.class, MAPPER);
    val trace = taskEvent.getTrace();
    val doc = DocumentConverter.buildTaskDocument(taskEvent);

    assertEquals(taskEvent.getRunId(), doc.getRunId());
    assertEquals(taskEvent.getRunName(), doc.getRunName());
    assertEquals(trace.getTask_id(), doc.getTaskId());
    assertEquals(trace.getName(), doc.getName());
    assertEquals(trace.getProcess(), doc.getProcess());
    assertEquals(trace.getTag(), doc.getTag());
    assertEquals(trace.getContainer(), doc.getContainer());
    assertEquals(trace.getAttempt(), doc.getAttempt());
    assertEquals(TaskState.QUEUED, doc.getState());
    assertEquals(trace.getSubmit(), doc.getSubmitTime());
    assertEquals(trace.getStart(), doc.getStartTime());
    assertEquals(trace.getComplete(), doc.getCompleteTime());
    assertEquals(trace.getExit(), doc.getExit());
    assertEquals(trace.getScript(), doc.getScript());
    assertEquals(trace.getWorkdir(), doc.getWorkdir());
    assertEquals(trace.getCpus(), doc.getCpus());
    assertEquals(trace.getMemory(), doc.getMemory());
    assertEquals(trace.getDuration(), doc.getDuration());
    assertEquals(trace.getRealtime(), doc.getRealtime());
  }
}
