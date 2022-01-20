/*
 * Copyright (c) 2020 The Ontario Institute for Cancer Research. All rights reserved
 *
 * This program and the accompanying materials are made available under the terms of the GNU Affero General Public License v3.0.
 * You should have received a copy of the GNU Affero General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.icgc_argo.workflow.relay.service;

import static org.icgc_argo.workflow.relay.util.Fixture.loadJsonFixture;
import static org.icgc_argo.workflow.relay.util.OffsetDateTimeDeserializer.getOffsetDateTimeModule;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.model.index.TaskState;
import org.icgc_argo.workflow.relay.model.index.WorkflowState;
import org.icgc_argo.workflow.relay.model.nextflow.TaskEvent;
import org.icgc_argo.workflow.relay.model.nextflow.WorkflowEvent;
import org.icgc_argo.workflow.relay.util.NextflowDocumentConverter;
import org.junit.Test;

@Slf4j
public class TestDocumentConverter {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .registerModule(getOffsetDateTimeModule())
          .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
          .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

  @Test
  public void testConvertWorkflowJson() {
    val workflowEvent =
        loadJsonFixture(
            this.getClass(), "nextflow_workflow_event.json", WorkflowEvent.class, MAPPER);

    val expected = workflowEvent.getMetadata().getWorkflow();
    val doc = NextflowDocumentConverter.buildWorkflowDocument(workflowEvent);
    assertEquals(workflowEvent.getRunName(), doc.getRunId());
    assertEquals(workflowEvent.getRunId(), doc.getSessionId());
    assertEquals(doc.getState(), WorkflowState.COMPLETE);
    assertEquals(workflowEvent.getMetadata().getParameters(), doc.getParameters());
    assertEquals(expected.getStart().toInstant(), doc.getStartTime());
    assertEquals(expected.getComplete().toInstant(), doc.getCompleteTime());
    assertEquals(expected.getRepository(), doc.getRepository());
    assertEquals(expected.getErrorReport(), doc.getErrorReport());
    assertEquals(expected.getExitStatus(), doc.getExitStatus());
    assertEquals(expected.getCommandLine(), doc.getCommandLine());
    assertEquals(expected.getLaunchDir(), doc.getEngineParameters().getLaunchDir());
    assertEquals(expected.getProjectDir(), doc.getEngineParameters().getProjectDir());
    assertEquals(expected.getWorkDir(), doc.getEngineParameters().getWorkDir());
    assertEquals(expected.getRevision(), doc.getEngineParameters().getRevision());
    assertEquals(expected.getResume(), doc.getEngineParameters().getResume());
  }

  @Test
  public void testConvertWorkflowJsonWithError() {
    val workflowEvent =
        loadJsonFixture(
            this.getClass(), "nextflow_workflow_event_error.json", WorkflowEvent.class, MAPPER);

    val expected = workflowEvent.getMetadata().getWorkflow();
    val doc = NextflowDocumentConverter.buildWorkflowDocument(workflowEvent);
    assertEquals(workflowEvent.getRunName(), doc.getRunId());
    assertEquals(workflowEvent.getRunId(), doc.getSessionId());
    assertEquals(doc.getState(), WorkflowState.EXECUTOR_ERROR);
    assertEquals(workflowEvent.getMetadata().getParameters(), doc.getParameters());
    assertEquals(expected.getStart().toInstant(), doc.getStartTime());
    assertEquals(expected.getComplete().toInstant(), doc.getCompleteTime());
    assertEquals(expected.getRepository(), doc.getRepository());
    assertEquals(expected.getErrorReport(), doc.getErrorReport());
    assertEquals(expected.getExitStatus(), doc.getExitStatus());
    assertEquals(expected.getCommandLine(), doc.getCommandLine());
    assertEquals(expected.getLaunchDir(), doc.getEngineParameters().getLaunchDir());
    assertEquals(expected.getProjectDir(), doc.getEngineParameters().getProjectDir());
    assertEquals(expected.getWorkDir(), doc.getEngineParameters().getWorkDir());
    assertEquals(expected.getRevision(), doc.getEngineParameters().getRevision());
    assertEquals(expected.getResume(), doc.getEngineParameters().getResume());
  }

  @Test
  public void testLoadTaskJson() {
    val taskEvent =
        loadJsonFixture(this.getClass(), "nextflow_task_event.json", TaskEvent.class, MAPPER);
    val trace = taskEvent.getTrace();
    val doc = NextflowDocumentConverter.buildTaskDocument(taskEvent);

    assertEquals(taskEvent.getRunName(), doc.getRunId());
    assertEquals(taskEvent.getRunId(), doc.getSessionId());
    assertEquals(trace.getTaskId(), doc.getTaskId());
    assertEquals(trace.getName(), doc.getName());
    assertEquals(trace.getProcess(), doc.getProcess());
    assertEquals(trace.getTag(), doc.getTag());
    assertEquals(trace.getContainer(), doc.getContainer());
    assertEquals(trace.getAttempt(), doc.getAttempt());
    assertEquals(TaskState.COMPLETE, doc.getState());
    assertEquals(trace.getSubmit(), doc.getSubmitTime());
    assertEquals(trace.getStart(), doc.getStartTime());
    assertEquals(trace.getComplete(), doc.getCompleteTime());
    assertEquals(trace.getExit(), doc.getExit());
    assertEquals(trace.getScript(), doc.getScript());
    assertEquals(trace.getWorkdir(), doc.getWorkdir());
    assertEquals(trace.getCpus(), doc.getCpus());
    assertEquals(trace.getMemory(), doc.getMemory());
    assertEquals(trace.getMemory(), doc.getMemory());
    assertEquals(trace.getRss(), doc.getRss());
    assertEquals(trace.getPeakRss(), doc.getPeakRss());
    assertEquals(trace.getVmem(), doc.getVmem());
    assertEquals(trace.getPeakVmem(), doc.getPeakVmem());
    assertEquals(trace.getReadBytes(), doc.getReadBytes());
    assertEquals(trace.getWriteBytes(), doc.getWriteBytes());
    assertEquals(trace.getDuration(), doc.getDuration());
    assertEquals(trace.getRealtime(), doc.getRealtime());
  }

  @Test
  public void testParamsMerging() {
    val oldParams =
        Map.of(
            "scoreMem", 5,
            "nestedParamOne", Map.of("valueOfThing", "asdf"),
            "nested-param-two", Map.of("valueOfThing", "asdf"));
    val newParams =
        Map.of(
            "scoreMem", 5,
            "score-mem", 5,
            "nestedParamOne", Map.of("valueOfThing", "asdf"),
            "nested-param-one", Map.of("valueOfThing", "asdf"),
            "nestedParamTwo", Map.of("valueOfThing", "asdf"),
            "nested-param-two", Map.of("valueOfThing", "asdf"));

    val result = NextflowDocumentConverter.mergeParams(oldParams, newParams);
    assertEquals(oldParams, result);
  }
}
