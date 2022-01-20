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

package org.icgc_argo.workflow.relay.util;

import static org.icgc_argo.workflow.relay.exceptions.NotFoundException.checkNotFound;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.CaseUtils;
import org.icgc_argo.workflow.relay.model.index.*;
import org.icgc_argo.workflow.relay.model.nextflow.TaskEvent;
import org.icgc_argo.workflow.relay.model.nextflow.WorkflowEvent;

/** Utility class that converts metadata POJOs to index POJOs. */
@Slf4j
@NoArgsConstructor
public class NextflowDocumentConverter {

  public static WorkflowDocument buildWorkflowDocument(@NonNull WorkflowEvent workflowEvent) {
    return buildWorkflowDocument(workflowEvent, null);
  }

  public static WorkflowDocument buildWorkflowDocument(
      @NonNull WorkflowEvent workflowEvent, WorkflowDocument oldDocument) {
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

    val parameters =
        oldDocument != null
            ? mergeParams(oldDocument.getParameters(), workflowEvent.getMetadata().getParameters())
            : workflowEvent.getMetadata().getParameters();

    val doc =
        WorkflowDocument.builder()
            .runId(workflowEvent.getRunName())
            .sessionId(workflowEvent.getRunId())
            .state(WorkflowState.fromNextflowEventAndSuccess(workflowEvent.getEvent(), success))
            .parameters(parameters)
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

  public static Map<String, Object> mergeParams(
      Map<String, Object> originalParams, Map<String, Object> newParams) {
    val newKeysToIgnore =
        newParams.keySet().stream()
            .filter(k -> k.contains("-"))
            .flatMap(
                k -> {
                  val camelCase = kebabToCamelCase(k);
                  if (originalParams.containsKey(k)) {
                    return Stream.of(camelCase);
                  } else if (originalParams.containsKey(camelCase)) {
                    return Stream.of(k);
                  }
                  return Stream.empty();
                })
            .collect(Collectors.toUnmodifiableSet());

    val merged = new HashMap<>(newParams);
    newKeysToIgnore.forEach(merged::remove);
    return merged;
  }

  private static String kebabToCamelCase(String s) {
    return CaseUtils.toCamelCase(s, false, '-');
  }
}
