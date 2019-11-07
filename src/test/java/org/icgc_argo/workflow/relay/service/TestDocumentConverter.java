package org.icgc_argo.workflow.relay.service;

import static org.icgc_argo.workflow.relay.util.Fixture.loadJsonFixture;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.entities.metadata.TaskEvent;
import org.icgc_argo.workflow.relay.entities.metadata.WorkflowEvent;
import org.icgc_argo.workflow.relay.util.DocumentConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
public class TestDocumentConverter {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void testConvertWorkflowJson() {
    val workflowEvent =
        loadJsonFixture(this.getClass(), "workflow_event.json", WorkflowEvent.class, MAPPER);
    val expected = workflowEvent.getMetadata().getWorkflow();
    val doc = DocumentConverter.buildWorkflowDocument(workflowEvent);

    assertEquals(workflowEvent.getRunId(), doc.getRunId());
    assertEquals(workflowEvent.getRunName(), doc.getRunName());
    assertEquals(expected.getStart(), doc.getStartTime());
    assertEquals(expected.getComplete(), doc.getCompleteTime());
    assertEquals(expected.getErrorReport(), doc.getErrorReport());
    assertEquals(expected.getExitStatus(), doc.getExitStatus());
    assertEquals(expected.getCommandLine(), doc.getCommandLine());
  }

  @Test
  public void testLoadTaskJson() {
    val taskEvent = loadJsonFixture(this.getClass(), "task_event.json", TaskEvent.class, MAPPER);
    val expected = taskEvent.getTrace();
    val doc = DocumentConverter.buildTaskDocument(taskEvent);

    assertEquals(taskEvent.getRunId(), doc.getRunId());
    assertEquals(taskEvent.getRunName(), doc.getRunName());
    assertEquals(expected.getComplete(), doc.getCompleteTime());
    assertEquals(expected.getStart(), doc.getStartTime());
    assertEquals(expected.getScript(), doc.getScript());
    assertEquals(expected.getExit(), doc.getExit());
    assertEquals(expected.getName(), doc.getName());
  }
}